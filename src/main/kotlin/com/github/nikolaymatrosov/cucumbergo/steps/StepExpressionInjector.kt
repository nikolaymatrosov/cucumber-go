package com.github.nikolaymatrosov.cucumbergo.steps

import com.github.nikolaymatrosov.cucumbergo.StepUtils
import com.goide.GoTypes
import com.goide.psi.GoCallExpr
import com.intellij.lang.injection.general.Injection
import com.intellij.lang.injection.general.LanguageInjectionContributor
import com.intellij.lang.injection.general.SimpleInjection
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.intellij.lang.regexp.RegExpLanguage


internal class StepExpressionInjector : LanguageInjectionContributor {
    override fun getInjection(context: PsiElement): Injection? {
        if (context.elementType != GoTypes.STRING_LITERAL || !StepUtils.isStepDefinition(PsiTreeUtil.getParentOfType(context, GoCallExpr::class.java)!!)) {
            return null
        }

        return SimpleInjection(
            RegExpLanguage.INSTANCE, "", "", null
        )
    }


}