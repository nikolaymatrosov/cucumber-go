package com.github.nikolaymatrosov.cucumbergo.search

import com.github.nikolaymatrosov.cucumbergo.StepDeclaration
import com.goide.psi.GoArgumentList
import com.goide.psi.GoCallExpr
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.pom.PomDeclarationSearcher
import com.intellij.pom.PomTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.Consumer


class StepDeclarationSearcher : PomDeclarationSearcher() {

    fun <T> inReadAction(body: () -> T): T {
        return ApplicationManager.getApplication().run {
            if (isReadAccessAllowed) {
                body()
            } else runReadAction<T>(body)
        }
    }

    private fun getStepDeclaration(element: PsiElement, stepName: String): StepDeclaration? {
        return CachedValuesManager.getCachedValue(element) {
            CachedValueProvider.Result.create(StepDeclaration(element, stepName), element)
        }
    }

    override fun findDeclarationsAt(element: PsiElement, offsetInElement: Int, consumer: Consumer<in PomTarget>) {
        val injectionHost = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element) ?: element

        ProgressManager.checkCanceled()

        val stepDeclaration = inReadAction {
            (injectionHost.parent as? GoCallExpr)?.let { candidate ->
                (element as? GoArgumentList)?.let { elem ->
                    val keyword = candidate.children[0].lastChild.text
                    var stepName = elem.expressionList.getOrNull(0)?.text
                    if (listOf("Given", "When", "Then", "Step").contains(keyword) && stepName != null) {
                        stepName = stepName.replace("`", "")
                        getStepDeclaration(candidate, "$keyword $stepName")
                    } else {
                        null
                    }
                }
            }
        }

        stepDeclaration?.let {
            consumer.consume(it)
        }
    }
}