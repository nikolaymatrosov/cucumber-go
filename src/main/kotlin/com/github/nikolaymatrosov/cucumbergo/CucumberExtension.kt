package com.github.nikolaymatrosov.cucumbergo

import com.github.nikolaymatrosov.cucumbergo.steps.StepDefinition
import com.goide.GoFileType
import com.goide.psi.GoCallExpr
import com.goide.psi.GoFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.plugins.cucumber.BDDFrameworkType
import org.jetbrains.plugins.cucumber.StepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import com.github.nikolaymatrosov.cucumbergo.steps.StepDefinitionCreator as GoStepDefinitionCreator

class CucumberExtension : AbstractCucumberExtension() {
    override fun isStepLikeFile(child: PsiElement): Boolean {
        return child is GoFile
    }

    override fun isWritableStepLikeFile(child: PsiElement): Boolean {
        return (child as? GoFile)?.containingFile?.virtualFile?.isWritable ?: false
    }

    override fun getStepFileType(): BDDFrameworkType {
        return BDDFrameworkType(GoFileType.INSTANCE)
    }

    override fun getStepDefinitionCreator(): StepDefinitionCreator {
        return GoStepDefinitionCreator()
    }

    override fun loadStepsFor(featureFile: PsiFile?, module: Module): List<AbstractStepDefinition> {
        val fileBasedIndex = FileBasedIndex.getInstance()
        val project = module.project
        // Use allScope to include Go module dependencies (library sources in the module cache)
        val scope = GlobalSearchScope.allScope(project)
        val result = mutableListOf<AbstractStepDefinition>()

        fileBasedIndex.processValues(INDEX_ID, true, null, { file, value ->
            ProgressManager.checkCanceled()
            val psiFile = PsiManager.getInstance(project).findFile(file)
            if (psiFile == null) {
                true
            } else {
                for (offset in value) {
                    val element = psiFile.findElementAt(offset + 1)
                    val stepDefPsi = PsiTreeUtil.getParentOfType(element, GoCallExpr::class.java)
                    stepDefPsi?.let {
                        result.add(StepDefinition(stepDefPsi))
                    }
                }
                true
            }
        }, scope)

        return result
    }

    override fun getStepDefinitionContainers(featureFile: GherkinFile): Collection<PsiFile> {
        val module = ModuleUtilCore.findModuleForPsiElement(featureFile)
        val steps = module?.let { mod ->
            loadStepsFor(featureFile, mod)
        }
        // Include all files with step definitions (both writable project files and read-only library files)
        val psiFiles = steps
            ?.mapNotNull { step -> step.element?.containingFile }
            ?.filter { file -> file is GoFile }
            ?.distinct()
            ?: emptyList()
        return psiFiles
    }
}