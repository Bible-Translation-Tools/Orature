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
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ComboboxItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.DeviceComboboxCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.LanguageComboboxCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class SettingsView : View() {
    private val viewModel: SettingsViewModel by inject()

    private val addPluginDialog: AddPluginDialog = find<AddPluginDialog>().apply {
        orientationProperty.set(viewModel.orientationProperty.value)
    }

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
                            tooltip(messages["close"])
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

                    combobox(viewModel.selectedLocaleLanguageProperty, viewModel.supportedLocaleLanguages) {
                        addClass("wa-combobox")
                        fitToParentWidth()

                        tooltip {
                            textProperty().bind(
                                this@combobox.selectionModel.selectedItemProperty().stringBinding { it?.name }
                            )
                        }

                        visibleRowCount = 5

                        cellFormat {
                            val view = ComboboxItem()
                            graphic = view.apply {
                                topTextProperty.set(it.name)
                                bottomTextProperty.set(it.anglicizedName)
                            }
                        }

                        buttonCell = LanguageComboboxCell()
                    }
                }

                label(messages["audioSettings"]).apply {
                    addClass("app-drawer__subtitle")
                }

                vbox {
                    addClass("app-drawer__section")

                    label(messages["playbackSettings"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    
                    combobox(viewModel.selectedOutputDeviceProperty, viewModel.outputDevices) {
                        addClass("wa-combobox")
                        fitToParentWidth()

                        tooltip {
                            textProperty().bind(this@combobox.selectionModel.selectedItemProperty())
                        }

                        cellFormat {
                            val view = ComboboxItem()
                            graphic = view.apply {
                                topTextProperty.set(it)
                            }
                        }

                        buttonCell = DeviceComboboxCell(FontIcon(MaterialDesign.MDI_VOLUME_HIGH))

                        selectionModel.selectedItemProperty().onChange {
                            it?.let { viewModel.updateOutputDevice(it) }
                        }
                    }

                    label(messages["recordSettings"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    combobox(viewModel.selectedInputDeviceProperty, viewModel.inputDevices) {
                        addClass("wa-combobox")
                        fitToParentWidth()

                        tooltip {
                            textProperty().bind(this@combobox.selectionModel.selectedItemProperty())
                        }

                        cellFormat {
                            val view = ComboboxItem()
                            graphic = view.apply {
                                topTextProperty.set(it)
                            }
                        }

                        buttonCell = DeviceComboboxCell(FontIcon(MaterialDesign.MDI_MICROPHONE))

                        selectionModel.selectedItemProperty().onChange {
                            it?.let { viewModel.updateInputDevice(it) }
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

                        label(messages["applicationName"]).apply {
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
                        tooltip {
                            textProperty().bind(this@apply.textProperty())
                        }
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

        // Devices are refreshed on dock and on drawer event otherwise it is not loaded the first time.
        subscribe<DrawerEvent<UIComponent>> {
            viewModel.refreshDevices()
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.bind()
        viewModel.refreshDevices()
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }

    private fun initChangeLanguageDialog() {
        val successDialog = confirmdialog {
            titleTextProperty.set(messages["settings"])
            messageTextProperty.set(messages["changeLanguageSuccessMessage"])
            orientationProperty.set(viewModel.orientationProperty.value)

            cancelButtonTextProperty.set(messages["close"])
            onCloseAction { viewModel.showChangeLanguageSuccessDialogProperty.set(false) }
            onCancelAction { viewModel.showChangeLanguageSuccessDialogProperty.set(false) }
        }

        viewModel.showChangeLanguageSuccessDialogProperty.onChange {
            Platform.runLater { if (it) successDialog.open() else successDialog.close() }
        }
    }
}
