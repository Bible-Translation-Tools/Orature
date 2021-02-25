package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.AddPluginStyles
import tornadofx.*

class AddPluginView : View() {
    private var nameField: JFXTextField by singleAssign()
    private var executableField: JFXTextField by singleAssign()
    private var canEditBox: JFXCheckBox by singleAssign()
    private var canRecordBox: JFXCheckBox by singleAssign()
    init {
        title = messages["addPlugin"]
        importStylesheet<AddPluginStyles>()
    }

    private val viewModel: AddPluginViewModel by inject()

    override val root = form {
        fieldset {
            field {
                nameField = JFXTextField().apply {
                    isLabelFloat = true
                    promptText = messages["name"]
                    bind(viewModel.nameProperty)
                    validator { viewModel.validateName() }
                }
                add(nameField)
            }
            field {
                executableField = JFXTextField().apply {
                    isLabelFloat = true
                    promptText = messages["executable"]
                    bind(viewModel.pathProperty)
                    validator { viewModel.validatePath() }
                }
                add(executableField)
                add(JFXButton(messages["browse"].toUpperCase()).apply {
                    addClass(AddPluginStyles.browseButton)
                    action {
                        val files = chooseFile(
                                messages["chooseExecutable"],
                                arrayOf(),
                                mode = FileChooserMode.Single
                        )
                        if (files.isNotEmpty()) {
                            viewModel.path = files.single().toString()
                        }
                    }
                })
            }
            field {
                canEditBox = JFXCheckBox(messages["canRecord"])
                        .apply { selectedProperty().bindBidirectional(viewModel.canRecordProperty) }
                add(canEditBox)
                canRecordBox = JFXCheckBox(messages["canEdit"])
                        .apply { selectedProperty().bindBidirectional(viewModel.canEditProperty) }
                add(canRecordBox)
            }
        }
        hbox {
            alignment = Pos.TOP_RIGHT
            add(JFXButton(messages["save"].toUpperCase()).apply {
                addClass(AddPluginStyles.saveButton)
                action {
                    viewModel.save()
                    close()
                }
                enableWhen { viewModel.validated() }
            })
        }
    }

    override fun onDock() {
        super.onDock()
        nameField.requestFocus()
    }

    override fun onUndock() {
        super.onUndock()
        canEditBox.isSelected = false
        canRecordBox.isSelected = false
        nameField.text = ""
        executableField.text = ""
    }
}
