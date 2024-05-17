package com.github.nikolaymatrosov.cucumbergo.godog

import com.goide.execution.testing.GoTestEventsJsonConverter
import com.intellij.execution.testframework.TestConsoleProperties

class GodogEventsConverter(s: String, consoleProperties: TestConsoleProperties) : GoTestEventsJsonConverter(
    "Godog", s, consoleProperties
)