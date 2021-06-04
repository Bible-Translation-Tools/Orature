package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.CheckboxButton
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AddPluginViewModel
import tornadofx.*

class AddPluginDialog : OtterDialog() {

    private val viewModel: AddPluginViewModel by inject()

    private val content = VBox().apply {
        addClass("add-plugin-dialog")
        hbox {
            addClass("add-plugin-dialog__header")
            label(messages["addApp"]).apply {
                addClass("add-plugin-dialog__title")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("add-plugin-dialog__btn--close")
                graphic = FontIcon("gmi-close")
                action { close() }
            }
        }
        vbox {
            addClass("add-plugin-dialog__info")
            label(messages["thirdPartySupport"]) {
                addClass("add-plugin-dialog__subtitle")
            }
            label(messages["addAppDescription"]) {
                addClass("add-plugin-dialog__text")
            }
        }
        vbox {
            addClass("add-plugin-dialog__inputs")
            label(messages["applicationName"]) {
                addClass("add-plugin-dialog__label")
            }
            textfield {
                addClass("txt-input")
                promptTextProperty().set(messages["applicationNamePrompt"])
                viewModel.nameProperty.bindBidirectional(textProperty())
            }
            label(messages["filePath"]) {
                addClass("add-plugin-dialog__label")
            }
            hbox {
                addClass("add-plugin-dialog__bottom-controls")
                textfield {
                    addClass("txt-input")
                    hgrow = Priority.ALWAYS
                    textProperty().bindBidirectional(viewModel.pathProperty)
                }
                button(messages["browse"]) {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                    action {
                        val files = chooseFile(
                            messages["chooseExecutable"],
                            arrayOf(),
                            mode = FileChooserMode.Single
                        )
                        if (files.isNotEmpty()) {
                            val finalPath = viewModel.completePluginPath(files.single().toString())
                            viewModel.pathProperty.set(finalPath)
                        }
                    }
                }
            }
        }
        hbox {
            addClass("add-plugin-dialog__checkboxes")
            add(
                CheckboxButton().apply {
                    hgrow = Priority.ALWAYS

                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                    text = messages["canRecord"]
                    viewModel.canRecordProperty.bindBidirectional(selectedProperty())
                }
            )
            add(
                CheckboxButton().apply {
                    hgrow = Priority.ALWAYS

                    graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                    text = messages["canEdit"]
                    viewModel.canEditProperty.bindBidirectional(selectedProperty())
                }
            )
        }
        button(messages["addApp"]) {
            addClass("btn", "btn--primary")
            graphic = FontIcon(MaterialDesign.MDI_PLUS)
            disableProperty().bind(viewModel.validProperty.not())

            action {
                viewModel.save()
                close()
            }
        }
    }

    init {
        setContent(content)
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.nameProperty.set("")
        viewModel.pathProperty.set("")
        viewModel.canEditProperty.set(false)
        viewModel.canRecordProperty.set(false)
    }
}
