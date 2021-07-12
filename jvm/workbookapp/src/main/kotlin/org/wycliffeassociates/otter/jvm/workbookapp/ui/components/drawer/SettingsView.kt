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
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class SettingsView : View() {
    private val viewModel: SettingsViewModel by inject()

    private val addPluginDialog: AddPluginDialog = find()

    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["settings"]).apply {
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

                label(messages["interfaceSettings"]).apply {
                    addClass("app-drawer__subtitle")
                }

                vbox {
                    addClass("app-drawer__section")

                    label(messages["languageSettings"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    combobox(viewModel.selectedLocaleProperty, viewModel.supportedLocales) {
                        addClass("dropdown")
                        fitToParentWidth()

                        cellFormat {
                            graphic = Label().apply {
                                text = it.displayName.capitalize()
                                graphic = FontIcon(MaterialDesign.MDI_WEB)
                            }
                        }

                        selectionModel.selectedItemProperty().onChange {
                            it?.let { viewModel.updateLocale(it) }
                        }
                    }
                }

                label(messages["appSettings"]).apply {
                    addClass("app-drawer__subtitle")
                }

                vbox {
                    addClass("app-drawer__section")

                    hbox {
                        addClass("app-drawer__plugin-header")

                        label(messages["name"]).apply {
                            addClass("app-drawer__text")
                            hgrow = Priority.ALWAYS
                        }

                        region { hgrow = Priority.ALWAYS }

                        label {
                            addClass("app-drawer__plugin-header__icon")
                            graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        }

                        label {
                            addClass("app-drawer__plugin-header__icon")
                            graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                        }
                    }

                    vbox {
                        addClass("app-drawer__plugin-list")

                        val recorderToggleGroup = ToggleGroup()
                        val editorToggleGroup = ToggleGroup()

                        bindChildren(viewModel.audioPlugins) { pluginData ->
                            hbox {
                                addClass("app-drawer__plugin")

                                label(pluginData.name).apply {
                                    addClass("app-drawer__text")
                                    hgrow = Priority.ALWAYS
                                }

                                region { hgrow = Priority.ALWAYS }

                                radiobutton {
                                    isDisable = !pluginData.canRecord
                                    viewModel.selectedRecorderProperty.onChangeAndDoNow { selectedData ->
                                        isSelected = selectedData == pluginData
                                    }
                                    selectedProperty().onChange { selected ->
                                        if (selected) viewModel.selectRecorder(pluginData)
                                    }
                                    toggleGroup = recorderToggleGroup
                                }

                                radiobutton {
                                    isDisable = !pluginData.canEdit
                                    viewModel.selectedEditorProperty.onChangeAndDoNow { selectedData ->
                                        isSelected = selectedData == pluginData
                                    }
                                    selectedProperty().onChange { selected ->
                                        if (selected) viewModel.selectEditor(pluginData)
                                    }
                                    toggleGroup = editorToggleGroup
                                }
                            }
                        }
                    }

                    label(messages["addApp"]).apply {
                        addClass("app-drawer__text--link")
                        graphic = FontIcon(MaterialDesign.MDI_PLUS)
                        setOnMouseClicked {
                            addPluginDialog.open()
                        }
                    }
                }
            }
        }
    }

    init {
        importStylesheet(resources.get("/css/app-drawer.css"))
        importStylesheet(resources.get("/css/add-plugin-dialog.css"))
        importStylesheet(resources.get("/css/confirm-dialog.css"))
        viewModel.refreshPlugins()

        initChangeLanguageDialog()
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }

    private fun initChangeLanguageDialog() {
        val successDialog = confirmdialog {
            titleTextProperty.set(messages["settings"])
            messageTextProperty.set(messages["changeLanguageSuccessMessage"])

            cancelButtonTextProperty.set(messages["close"])
            onCloseAction { viewModel.showChangeLanguageSuccessDialogProperty.set(false) }
            onCancelAction { viewModel.showChangeLanguageSuccessDialogProperty.set(false) }
        }

        viewModel.showChangeLanguageSuccessDialogProperty.onChange {
            Platform.runLater { if (it) successDialog.open() else successDialog.close() }
        }
    }
}
