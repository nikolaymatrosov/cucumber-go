package com.github.nikolaymatrosov.cucumbergo

import com.goide.GoFileType
import com.goide.GoTypes
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.RecursiveLighterASTNodeWalkingVisitor
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import org.jetbrains.plugins.cucumber.CucumberStepIndex

class CucumberGoStepIndex : CucumberStepIndex() {

    override fun getName(): ID<Boolean, List<Int>> {
        return INDEX_ID
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(GoFileType.INSTANCE)
    }

    override fun getPackagesToScan(): Array<String> {
        return PACKAGES
    }


    override fun getStepDefinitionOffsets(lighterAst: LighterAST, text: CharSequence): List<Int> {
        val result = mutableListOf<Int>()

        val visitor: RecursiveLighterASTNodeWalkingVisitor =
            object : RecursiveLighterASTNodeWalkingVisitor(lighterAst) {
                override fun visitNode(element: LighterASTNode) {
                    if (element.tokenType == GoTypes.IDENTIFIER && StepUtils.checkIdentifierName(element.toString())) {
                        result.add(element.startOffset)
                    }

                    super.visitNode(element)
                }
            }

        visitor.visitNode(lighterAst.root)

        return result
    }


}

val INDEX_ID = ID.create<Boolean, List<Int>>("go.cucumber.step")
val GODOG_PACKAGE = "github.com/cucumber/godog"
val PACKAGES = arrayOf(GODOG_PACKAGE)