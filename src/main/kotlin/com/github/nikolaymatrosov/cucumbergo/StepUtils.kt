package com.github.nikolaymatrosov.cucumbergo

import com.intellij.openapi.application.ApplicationManager
import kotlin.collections.joinToString

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

fun ToCamelCase(s: String): String {
    return s
        .split(" ")
        .joinToString("") { s ->
            s.replaceFirstChar { ch ->
                ch.uppercase()
            }
        }
}