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
        if (!validCandidate(element)) {
            return null
        }
        val textElement = element.children[1].children[0]
        val stepName = textElement.text.replace("`", "")
        return LineMarkerInfo(
            textElement.firstChild,
            textElement.textRange,
            Cucumber,
            { stepName },
            null,
            RIGHT,
            { stepName })

    }

    private fun validCandidate(element: PsiElement) =
        element is GoCallExpr && element.children.size == 2 && keywords.contains(element.children[0].lastChild.text)
}