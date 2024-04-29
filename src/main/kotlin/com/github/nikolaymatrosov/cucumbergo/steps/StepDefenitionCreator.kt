package com.github.nikolaymatrosov.cucumbergo.steps

import ai.grazie.utils.capitalize
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.impl.GoElementFactory
import com.intellij.openapi.application.runWriteAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinStep


class StepDefinitionCreator : AbstractStepDefinitionCreator() {
    override fun getDefaultStepDefinitionFolderPath(step: GherkinStep): String {
        return getDefaultGoStepDefinitionFolderPath(step)?.virtualFile?.path
            ?: (step.containingFile.containingDirectory.virtualFile.path + "/steps")
    }

    private fun getDefaultGoStepDefinitionFolderPath(step: GherkinStep): PsiDirectory? {
        val featureDir = step.containingFile.containingDirectory
        val featureFile = step.containingFile
        var stepsDir: PsiDirectory? = null
        if (featureDir != null) {
            stepsDir = featureDir.findSubdirectory("steps")
            if (stepsDir != null) {
                return featureFile.getManager().findDirectory(stepsDir.virtualFile)
            }
        }
        return stepsDir
    }

    override fun createStepDefinitionContainer(directory: PsiDirectory, name: String): PsiFile {

        val featureName = name.replace("_test.go", "")
        val file = runWriteAction { directory.createFile(name) } as GoFile

        val newLines = GoElementFactory.createNewLine(file.project, 2)
        val featureStruct = GoElementFactory.createTypeDeclaration(
            file.project,
            "${featureName.capitalize()}Feature", GoElementFactory.createType(
                file.project,
                "struct {}",
                file
            )
        )

        runWriteAction {
            file.add(GoElementFactory.createFileFromText(file.project, "package steps").getPackage()!!)
            file.add(newLines)
            file.add(GoElementFactory.createImportDeclaration(file, "testing", "", true))
            file.add(GoElementFactory.createImportDeclaration(file, "github.com/cucumber/godog", "", true))
            file.add(newLines)
            file.add(featureStruct)
            file.add(newLines)
            file.add(createTestDefinition(file, featureName))
            file.add(newLines)
            file.add(createInitializeScenario(file, featureName))
        }

        return file
    }

    override fun getDefaultStepFileName(step: GherkinStep): String {
        val basename = step.containingFile?.name?.replace(".feature", "") ?: "Cucumber"
        return "${basename}_test.go"
    }


    override fun createStepDefinition(step: GherkinStep, file: PsiFile, withTemlpate: Boolean): Boolean {
        val stepText = step.substitutedName
        val stepName = toLowerCamelCaseName(step.name)

        val stepSignature = StringBuilder()
        stepSignature.append("(")

        for (arg in step.paramsSubstitutions) {
            stepSignature.append(arg)
            stepSignature.append(" string,")
        }
        stepSignature.append(") error")
        val current = PsiTreeUtil.collectElementsOfType(file, GoFunctionDeclaration::class.java)
            .find {
                it.name == stepName
            }
        if (current == null) {
            runWriteAction {
                (file as GoFile).add(
                    GoElementFactory.createFunctionDeclaration(
                        file.project,
                        stepName,
                        stepSignature.toString(),
                        "{\nreturn godog.ErrPending\n}",
                        file
                    )
                )
            }
        }

        val initializer = PsiTreeUtil.collectElementsOfType(file, GoFunctionDeclaration::class.java)
            .find {
                it.signature?.parameters?.parameterCount == 1
                        && it.signature?.parameters?.getDefinitionByIndex(0)
                    ?.getGoType(null)?.presentationText == "*godog.ScenarioContext"
            }

        runWriteAction {
            initializer?.block?.addBefore(
                GoElementFactory.createStatement(file.project, "ctx.Step(`^$stepText$`, $stepName)"),
                initializer.block?.lastChild
            )
        }

        file.navigate(true)
        return true
    }

    fun toLowerCamelCaseName(s: String): String {
        return s.split(" ")
            .map {
                it.replace("[\"<>.,!]".toRegex(), "")
            }
            .joinToString("") {
                it.replaceFirstChar {
                    it.uppercase()
                }
            }
            .replaceFirstChar { it.lowercase() }
    }

    private fun createTestDefinition(file: PsiFile, featureName: String): PsiElement {
        return GoElementFactory.createFunctionDeclaration(
            file.project,
            "Test${featureName.capitalize()}",
            "(t *testing.T)",
            """
                {
                    suite := godog.TestSuite{
                        ScenarioInitializer: ${scenarioInitailizerName(featureName)},
                        Options: &godog.Options{
                            Format:   "pretty",
                            Paths:    []string{"../${featureName}.feature"},
                            TestingT: t, // Testing instance that will run subtests.
                        },
                    }
                    
                    if suite.Run() != 0 {
                        t.Fatal("non-zero status returned, failed to run feature tests")
                    }
                }
            """.trimIndent(),
            file
        )
    }

    private fun createInitializeScenario(file: PsiFile, featureName: String): PsiElement {
        return GoElementFactory.createFunctionDeclaration(
            file.project,
            scenarioInitailizerName(featureName),
            "(ctx *godog.ScenarioContext)",
            """
                {
                    
                }
            """.trimIndent(),
            file
        )
    }

    private fun scenarioInitailizerName(featureName: String): String {
        return "Initialize${featureName.capitalize()}Scenario"
    }
}