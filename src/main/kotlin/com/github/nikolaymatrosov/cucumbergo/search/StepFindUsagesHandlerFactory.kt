package com.github.nikolaymatrosov.cucumbergo.search

import com.github.nikolaymatrosov.cucumbergo.StepDeclaration
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiElement


class StepFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        if (element is PomTargetPsiElement) {
            if (element.target is StepDeclaration) {
                return true
            }
        }
        return false
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return object : FindUsagesHandler(element) {}
    }
}