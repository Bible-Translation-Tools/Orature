package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.SelectRadioButton
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.RemovePluginsDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class SettingsView : View() {
    private val viewModel: SettingsViewModel by inject()

    init {
        viewModel.refreshPlugins()
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
                            action { collapse() }
                        }
                    )
                }

                label(messages["appSettings"]).apply {
                    addClass("app-drawer__subtitle")
                }

                vbox {
                    addClass("app-drawer__section")

                    label(messages["recordIn"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }

                    vbox {
                        val recordingPluginGroup = ToggleGroup()
                        // Will need to refactor to using listview
                        viewModel.recorderPlugins.onChange {
                            children.clear()
                            it.list.map { pluginData ->
                                val radio = SelectRadioButton().apply {
                                    btnTextProperty.set(pluginData.name)
                                    viewModel.selectedRecorderProperty.onChange { data ->
                                        isSelected = data == pluginData
                                    }
                                    selectedProperty().onChange { selected ->
                                        if (selected) viewModel.selectRecorder(pluginData)
                                    }
                                    toggleGroup = recordingPluginGroup
                                }
                                children.add(radio)
                            }
                        }
                    }

                    hbox {
                        label("Add Application").apply {
                            addClass("app-drawer__text--link")
                            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                            setOnMouseClicked {
                                find<AddPluginDialog>().apply {
                                    openModal()
                                }
                            }
                        }
                    }
                }

                vbox {
                    addClass("app-drawer__section")

                    label(messages["editIn"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }

                    vbox {
                        val editPluginGroup = ToggleGroup()
                        viewModel.editorPlugins.onChange {
                            children.clear()
                            it.list.map { pluginData ->
                                val radio = SelectRadioButton().apply {
                                    btnTextProperty.set(pluginData.name)
                                    viewModel.selectedEditorProperty.onChange { data ->
                                        isSelected = data == pluginData
                                    }
                                    selectedProperty().onChange { selected ->
                                        if (selected) viewModel.selectEditor(pluginData)
                                    }
                                    toggleGroup = editPluginGroup
                                }
                                children.add(radio)
                            }
                        }
                    }

                    hbox {
                        label("Add Application").apply {
                            addClass("app-drawer__text--link")
                            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                            setOnMouseClicked {
                                find<AddPluginDialog>().apply {
                                    openModal()
                                }
                            }
                        }
                    }
                }

                hbox {
                    label("Remove Application").apply {
                        addClass("app-drawer__text--link")
                        graphic = FontIcon(MaterialDesign.MDI_CLOSE)
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
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
