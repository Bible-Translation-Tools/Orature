package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.stage.Stage
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.demo.ui.screens.RootView
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class WorkbookDemoApp : App(RootView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true

        tryImportStylesheet("/css/common.css")
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<LanguageTableDemoView>() // set the view for demo here
        workspace.root.apply {
            contextmenu {
                item("Change Theme") {
                    action { toggleTheme() }
                }
            }
        }
    }

    private fun toggleTheme() {
        if (workspace.root.hasClass(ColorTheme.LIGHT.styleClass)) {
            workspace.root.removeClass(ColorTheme.LIGHT.styleClass)
            workspace.root.addClass(ColorTheme.DARK.styleClass)
        } else {
            workspace.root.removeClass(ColorTheme.DARK.styleClass)
            workspace.root.addClass(ColorTheme.LIGHT.styleClass)
        }
    }
}

fun main(args: Array<String>) {
    launch<WorkbookDemoApp>(args)
}