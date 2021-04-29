package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.beans.binding.Bindings
import javafx.beans.binding.ListBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.jvm.controls.button.SelectRadioButton
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.AudioPlugin
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.RemovePluginsDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*
import java.util.concurrent.Callable

class SettingsView : View() {
    private val viewModel: SettingsViewModel by inject()

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
                            graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        }

                        label {
                            graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                        }
                    }

                    vbox {
                        val recorderToggleGroup = ToggleGroup()
                        val editorToggleGroup = ToggleGroup()

                        bindChildren(viewModel.allAudioPlugins()) { pluginData ->
                            hbox {
                                addClass("app-drawer__plugin-list")

                                label(pluginData.name).apply {
                                    addClass("app-drawer__text")
                                    hgrow = Priority.ALWAYS
                                }

                                region { hgrow = Priority.ALWAYS }

                                radiobutton {
                                    isDisable = !pluginData.canRecord
                                    viewModel.selectedRecorderProperty.onChange { selectedData ->
                                        isSelected = selectedData == pluginData
                                    }
                                    selectedProperty().onChange { selected ->
                                        if (selected) viewModel.selectRecorder(pluginData)
                                    }
                                    toggleGroup = recorderToggleGroup
                                }

                                radiobutton {
                                    isDisable = !pluginData.canEdit
                                    viewModel.selectedEditorProperty.onChange { selectedData ->
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
                        graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                        setOnMouseClicked {
                            find<AddPluginDialog>().apply {
                                openModal()
                            }
                        }
                    }

                    label(messages["RemoveApp"]).apply {
                        addClass("app-drawer__text--link")
                        graphic = FontIcon(MaterialDesign.MDI_RECYCLE)
                        setOnMouseClicked {
                            find<RemovePluginsDialog>().apply {
                                openModal()
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-drawer.css").toExternalForm())
        viewModel.refreshPlugins()
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
