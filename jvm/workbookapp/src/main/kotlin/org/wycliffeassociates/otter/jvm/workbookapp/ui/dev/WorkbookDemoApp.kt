/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        workspace.dock<WorkbookTableDemoView>() // set the view for demo here
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