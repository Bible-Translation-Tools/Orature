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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AppInfoViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view.UpdaterView
import tornadofx.*
import java.text.MessageFormat

class InfoView : View() {
    val info = AppInfo()
    private val viewModel: AppInfoViewModel by inject()

    private lateinit var closeButton: Button

    override val root = vbox {
        addClass("app-drawer__content")

        DrawerTraversalEngine(this)

        scrollpane {
            addClass("app-drawer__scroll-pane")

            vbox {
                isFitToWidth = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["information"]).apply {
                        addClass("app-drawer__title")
                    }
                    region { hgrow = Priority.ALWAYS }
                    button {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_CLOSE)
                        tooltip(messages["close"])
                        action { collapse() }
                        closeButton = this
                    }
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

                add<UpdaterView>()

                vbox {
                    addClass("app-drawer__section")

                    label(messages["applicationLogs"]).apply {
                        addClass("app-drawer__subtitle")
                    }
                    add(
                        JFXButton(messages["viewLogs"]).apply {
                            styleClass.addAll("btn", "btn--secondary")

                            tooltip {
                                textProperty().bind(this@apply.textProperty())
                            }

                            setOnAction {
                                viewModel.browseApplicationLog()
                            }
                        }
                    )
                }

                vbox {
                    addClass("app-drawer__section")

                    label(messages["errorReport"]).apply {
                        addClass("app-drawer__subtitle")
                    }
                    label(messages["errorReportDescription"]).apply {
                        fitToParentWidth()
                        addClass("app-drawer__text")
                    }

                    label(messages["description"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    textarea {
                        addClass("app-drawer__report-message")
                        textProperty().bindBidirectional(viewModel.errorDescription)
                        overrideDefaultKeyEventHandler {
                            viewModel.submitErrorReport()
                        }
                    }
                    label {
                        addClass("app-drawer__report-status")
                        visibleWhen(viewModel.reportTimeStamp.isNotNull)
                        managedWhen(visibleProperty())
                        textProperty().bind(viewModel.reportTimeStamp.stringBinding {
                            MessageFormat.format(messages["errorReportSent"], it)
                        })
                    }
                    add(
                        JFXButton(messages["sendErrorReport"]).apply {
                            styleClass.addAll("btn", "btn--secondary")
                            disableProperty().bind(viewModel.errorDescription.isEmpty)

                            tooltip {
                                textProperty().bind(this@apply.textProperty())
                            }

                            setOnAction {
                                viewModel.submitErrorReport()
                            }
                        }
                    )
                }
            }
        }

        setOnKeyReleased {
            if (it.code == KeyCode.ESCAPE) collapse()
        }
    }

    init {
        tryImportStylesheet(resources["/css/app-drawer.css"])

        subscribe<DrawerEvent<UIComponent>> {
            if (it.action == DrawerEventAction.OPEN) {
                focusCloseButton()
            }
        }
    }

    override fun onDock() {
        super.onDock()
        focusCloseButton()
    }

    private fun focusCloseButton() {
        runAsync {
            Thread.sleep(500)
            runLater(closeButton::requestFocus)
        }
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
