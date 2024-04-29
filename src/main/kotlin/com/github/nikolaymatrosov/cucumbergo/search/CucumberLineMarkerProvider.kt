package com.github.nikolaymatrosov.cucumbergo.search

import com.goide.psi.GoCallExpr
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.RIGHT
import com.intellij.psi.PsiElement
import icons.CucumberIcons.Cucumber

class CucumberLineMarkerProvider : LineMarkerProvider {
    private val keywords = listOf("Given", "When", "Then", "Step") // TODO: fix it
//    protected fun collectNavigationMarkers(
//        element: PsiElement,
//        result: MutableCollection<RelatedItemLineMarkerInfo<*>?>
//    ) {
//        if (element !is GoCallExpr) {
//            return
//        }
//        println(element.children[0].references)
//        if (element.children.size == 2 && keywords.contains(element.children[0].text)) {
//            val stepName = element.children[1].text
//            result.add(
//                RelatedItemLineMarkerInfo(
//                    element,
//                    element.textRange,
//                    Cucumber,
//                    { stepName },
//                    null,
//                    RIGHT,
//                    { stepName })
//            )
//        } else {
//            null
//        }
//    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is GoCallExpr) {
            return null
        }
        if (element.children.size == 2 && keywords.contains(element.children[0].lastChild.text)) {
            val stepName = element.children[1].children[0].text.replace("`", "")
            return LineMarkerInfo(element, element.textRange, Cucumber, { stepName }, null, RIGHT, { stepName })
        }
        return null
    }
}