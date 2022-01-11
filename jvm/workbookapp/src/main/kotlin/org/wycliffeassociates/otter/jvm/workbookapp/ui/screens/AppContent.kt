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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.geometry.Side
import javafx.util.Duration
import org.controlsfx.control.HiddenSidesPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEventAction
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class AppContent : View() {

    private val navigator: NavigationMediator by inject()
    private val rootViewModel: RootViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    override val root = HiddenSidesPane().apply {
        content =  stackpane {
            borderpane {
                top = navigator.breadCrumbsBar.apply {
                    orientationScaleProperty.bind(settingsViewModel.orientationScaleProperty)
                    disableWhen(rootViewModel.pluginOpenedProperty)
                }
                center<Workspace>()
            }
            pane {
                addClass("app-drawer__overlay")
                visibleProperty().bind(rootViewModel.drawerOpenedProperty)
                managedProperty().bind(visibleProperty())
                setOnMouseClicked {
                    fire(DrawerEvent(this@AppContent::class, DrawerEventAction.CLOSE))
                }
            }
        }

        triggerDistance = 0.0
        animationDelay = Duration.ZERO

        subscribe<DrawerEvent<UIComponent>> {
            pinnedSide = Side.LEFT

            when (it.action) {
                DrawerEventAction.OPEN -> {
                    // Wait until animation (if any) ends then open a drawer
                    Thread {
                        Thread.sleep(animationDuration.toMillis().toLong())
                        Platform.runLater {
                            rootViewModel.drawerOpenedProperty.set(true)
                            left = find(it.type).root
                            show(Side.LEFT)
                        }
                    }.start()
                }
                DrawerEventAction.CLOSE -> {
                    rootViewModel.drawerOpenedProperty.set(false)
                    hide()
                }
            }
        }
    }
}
