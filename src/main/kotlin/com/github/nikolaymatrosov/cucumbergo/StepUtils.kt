package com.github.nikolaymatrosov.cucumbergo

import com.goide.psi.GoCallExpr
import com.intellij.psi.PsiElement

class StepUtils {
    companion object {

        fun isStepDefinition(element: PsiElement): Boolean {
            if (element !is GoCallExpr) return false

            val keyword = element.children[0].lastChild.text

            return checkIdentifierName(keyword)
        }

        fun checkIdentifierName(name: String): Boolean {
            return name == "Step" || name == "Given" || name == "When" || name == "Then"
        }
    }
}