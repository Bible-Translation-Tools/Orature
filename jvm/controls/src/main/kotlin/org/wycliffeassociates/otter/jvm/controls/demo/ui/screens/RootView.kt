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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.DemoViewModel
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class RootView : View() {
    private val viewModel: DemoViewModel by inject()

    override val root = stackpane {
        prefWidth = 800.0
        prefHeight = 600.0

        borderpane {
            center<Workspace>()
        }
    }

    init {
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS

        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/control.css"])

        bindThemeClassToRoot()

        viewModel.updateTheme(ColorTheme.SYSTEM)
    }

    override fun onDock() {
        super.onDock()
        viewModel.bind()
    }

    private fun bindThemeClassToRoot() {
        viewModel.appColorMode.onChange {
            when (it) {
                ColorTheme.LIGHT -> {
                    root.addClass(ColorTheme.LIGHT.styleClass)
                    root.removeClass(ColorTheme.DARK.styleClass)
                }
                ColorTheme.DARK -> {
                    root.addClass(ColorTheme.DARK.styleClass)
                    root.removeClass(ColorTheme.LIGHT.styleClass)
                }
            }
        }
    }
}