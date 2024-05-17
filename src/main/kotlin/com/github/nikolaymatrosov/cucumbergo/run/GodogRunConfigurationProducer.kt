package com.github.nikolaymatrosov.cucumbergo.run

import ai.grazie.utils.capitalize
import com.github.nikolaymatrosov.cucumbergo.CucumberExtension
import com.github.nikolaymatrosov.cucumbergo.godog.GodogFramework
import com.goide.execution.GoBuildingRunConfiguration.Kind
import com.goide.execution.GoRunUtil
import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.execution.testing.GoTestRunConfigurationProducerBase
import com.goide.psi.GoFile
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinScenario
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline

class GodogRunConfigurationProducer protected constructor() :
    GoTestRunConfigurationProducerBase(GodogFramework.INSTANCE) {
    override fun setupConfigurationFromContext(
        configuration: GoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<*>
    ): Boolean {
        val element = sourceElement.get() as PsiElement
        if (element.containingFile is GherkinFile) {
            val file = element.containingFile.virtualFile

            // assuming all steps are in the same directory
            CucumberExtension().getStepDefinitionContainers(element.containingFile as GherkinFile).first().let {
                configuration.workingDirectory = it.virtualFile.parent.path
                configuration.`package` = (it as GoFile).getImportPath(false).toString()
            }

            configuration.testFramework = GodogFramework.INSTANCE
            configuration.kind = Kind.PACKAGE
            configuration.goToolParams = GoRunUtil.filterOutInstallParameter(configuration.goToolParams)
            val scenario = PsiTreeUtil.getParentOfType(
                element,
                GherkinScenario::class.java
            )

            val scenarioOutline = PsiTreeUtil.getParentOfType(
                element,
                GherkinScenarioOutline::class.java
            )

            var pattern = "^\\QTest" + file.nameWithoutExtension.capitalize() + "\\E\$"

            if (scenario != null) {
                pattern = pattern + "/^" + scenario.scenarioName + "$"
            } else if (scenarioOutline != null) {
                pattern = pattern + "/^" + scenarioOutline.scenarioName + "(#\\d+)?$"
            }
            configuration.pattern = pattern

            configuration.setGeneratedName()
            return true
        }
//        if (element is PsiDirectory) {
//            val dir = element.virtualFile
//            if (!FileTypeIndex.containsFileOfType(
//                    GherkinFileType.INSTANCE,
//                    GlobalSearchScopesCore.directoryScope(element.getProject(), dir, true)
//                )
//            ) {
//                return false
//            }
//            val directoryPath = dir.path
//            configuration.testFramework = GodogFramework.INSTANCE
//            configuration.directoryPath = directoryPath
//            configuration.workingDirectory = directoryPath
//            configuration.kind = Kind.DIRECTORY
//            configuration.goToolParams = GoRunUtil.filterOutInstallParameter(configuration.goToolParams)
//            configuration.setGeneratedName()
//
//            return true
//        }
        return false
    }
}