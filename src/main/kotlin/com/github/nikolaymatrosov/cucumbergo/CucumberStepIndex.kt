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

class CucumberStepIndex : CucumberStepIndex() {
    companion object {
        val INDEX_ID = ID.create<Boolean, List<Int>>("go.cucumber.step")
        val PACKAGES = arrayOf("github.com/cucumber/godog")
    }

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


    /*
    ctx.Step(`^I should see an error message "([^"]*)" (\d+)$`, iShouldSeeAnErrorMessage)
     */
    override fun getStepDefinitionOffsets(lighterAst: LighterAST, text: CharSequence): List<Int> {
        var result = mutableListOf<Int>()

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