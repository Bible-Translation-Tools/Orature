package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import javafx.application.Application
import tornadofx.Scope

class ParameterizedScope(
    val parameters: Application.Parameters,
    private val onNavigateBackCallback: () -> Unit
) : Scope() {
    fun navigateBack() {
        onNavigateBackCallback()
    }
}