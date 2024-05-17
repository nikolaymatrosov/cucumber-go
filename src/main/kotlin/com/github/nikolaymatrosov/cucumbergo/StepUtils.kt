package com.github.nikolaymatrosov.cucumbergo

import com.intellij.openapi.application.ApplicationManager

class StepUtils {
    companion object {
        fun checkIdentifierName(name: String): Boolean {
            return name == "Step" || name == "Given" || name == "When" || name == "Then"
        }
    }

}

fun <T> inReadAction(body: () -> T): T {
    return ApplicationManager.getApplication().run {
        if (isReadAccessAllowed) {
            body()
        } else runReadAction<T>(body)
    }
}