/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.AppBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import tornadofx.*

class RootView : View() {

    private val viewModel: RootViewModel by inject()

    init {
        // Configure the Workspace: sets up the window menu and external app open events

        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = true
            viewModel.pluginOpenedProperty.set(true)
        }
        workspace.subscribe<PluginClosedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = false
            viewModel.pluginOpenedProperty.set(false)
        }
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS

    }

    override val root = stackpane {
        borderpane {
            left<AppBar>()
            center<AppContent>()
        }
    }
}

