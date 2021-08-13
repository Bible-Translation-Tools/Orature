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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*

class InfoView : View() {
    val info = AppInfo()

    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["information"]).apply {
                        addClass("app-drawer__title")
                    }
                    region { hgrow = Priority.ALWAYS }
                    add(
                        JFXButton().apply {
                            addClass("app-drawer__btn--close")
                            graphic = FontIcon(MaterialDesign.MDI_CLOSE)
                            action { collapse() }
                        }
                    )
                }

                vbox {
                    addClass("app-drawer__section")
                    label(messages["aboutOrature"]).apply {
                        addClass("app-drawer__subtitle")
                    }

                    label(messages["aboutOratureDescription"]).apply {
                        fitToParentWidth()
                        addClass("app-drawer__text")
                    }
                }

                vbox {
                    addClass("app-drawer__section--filled")

                    label(messages["currentVersion"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    label(info.getVersion() ?: messages["na"]).apply {
                        addClass("app-drawer__text")
                    }
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-drawer.css").toExternalForm())
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
