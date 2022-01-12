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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RemovePluginsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.RemovePluginsStyles
import tornadofx.*

class RemovePluginsDialog : View() {
    private val viewModel: RemovePluginsViewModel by inject()

    init {
        importStylesheet<RemovePluginsStyles>()
    }

    override val root = stackpane {
        title = messages["remove"]
        addClass(RemovePluginsStyles.removePluginsRoot)
        label(messages["noPlugins"]) {
            addClass(RemovePluginsStyles.noPluginLabel)
            visibleProperty().bind(viewModel.noPluginsProperty)
            managedProperty().bind(visibleProperty())
        }
        listview(viewModel.plugins) {
            addClass(RemovePluginsStyles.pluginList)
            visibleProperty().bind(viewModel.noPluginsProperty.not())
            managedProperty().bind(visibleProperty())
            cellCache {
                hbox {
                    addClass(RemovePluginsStyles.pluginListCell)
                    label(it.name) {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                    }
                    add(JFXButton().apply {
                        graphic = RemovePluginsStyles.deleteIcon(20)
                        isDisableVisualFocus = true
                        addClass(RemovePluginsStyles.deleteButton)
                        action {
                            viewModel.remove(it)
                        }
                    })
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.refreshPlugins()
    }
}
