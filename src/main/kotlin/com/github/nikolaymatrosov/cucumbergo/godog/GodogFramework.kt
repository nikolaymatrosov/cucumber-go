package com.github.nikolaymatrosov.cucumbergo.godog

import com.github.nikolaymatrosov.cucumbergo.GODOG_PACKAGE
import com.github.nikolaymatrosov.cucumbergo.run.GodogRunningState
import com.goide.GoFileType
import com.goide.execution.testing.GoTestFramework
import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.execution.testing.GoTestRunningState
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile


class GodogFramework : GoTestFramework() {
    override fun getName(): String {
        return "Godog"
    }

    override fun isAvailable(module: Module?): Boolean {
        return true
    }

    override fun isAvailableOnFile(psiFile: PsiFile?): Boolean {
        return psiFile != null
                && psiFile.fileType === GoFileType.INSTANCE
                && (psiFile as GoFile).imports.any { it.path == GODOG_PACKAGE }
    }

    override fun isAvailableOnFunction(goFunctionOrMethodDeclaration: GoFunctionOrMethodDeclaration?): Boolean {
        return false
    }

    override fun supportsJsonTestsOutput(): Boolean {
        return true
    }

    override fun newRunningState(
        executionEnvironment: ExecutionEnvironment,
        module: Module,
        goTestRunConfiguration: GoTestRunConfiguration
    ): GoTestRunningState {
        return GodogRunningState(executionEnvironment, module, goTestRunConfiguration)
    }

    override fun createTestEventsConverter(
        s: String,
        testConsoleProperties: TestConsoleProperties,
        module: Module?
    ): OutputToGeneralTestEventsConverter {
        return GodogEventsConverter(s, testConsoleProperties)
    }

    override fun getPackageConfigurationName(packageName: String): String {
        return "Godog"
    }

    companion object {
        val INSTANCE: GodogFramework = GodogFramework()

        init {
            all().add(INSTANCE)
        }
    }
}