package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.cardRadioButton
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ConflictResolution
import tornadofx.*
import java.text.MessageFormat

class ImportConflictDialog : OtterDialog() {
    val projectNameProperty = SimpleStringProperty()
    val chaptersProperty = SimpleIntegerProperty()
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onSubmitActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val conflictResolutionProperty = SimpleObjectProperty<ConflictResolution>(ConflictResolution.OVERRIDE)

    private val content =
        VBox().apply {
            addClass("confirm-dialog")

            hbox {
                addClass("confirm-dialog__header")
                label {
                    textProperty().bind(
                        projectNameProperty.stringBinding {
                            MessageFormat.format(messages["bookNameImportTitle"], it)
                        },
                    )
                    addClass("h3")
                }
                region { hgrow = Priority.ALWAYS }
                button {
                    addClass("btn", "btn--icon", "btn--borderless")
                    graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                    tooltip(messages["close"])
                    onActionProperty().bind(onCloseActionProperty)
                }
            }

            vbox {
                addClass("confirm-dialog__body", "import-conflict-dialog__body")
                vgrow = Priority.ALWAYS

                label {
                    addClass("normal-text")
                    textProperty().bind(
                        chaptersProperty.stringBinding {
                            MessageFormat.format(messages["importConflictDescription"], it, projectNameProperty.value)
                        },
                    )
                }
                hbox {
                    addClass("confirm-dialog__control-gap")
                    vgrow = Priority.ALWAYS

                    val tg = ToggleGroup()
                    cardRadioButton(tg) {
                        hgrow = Priority.ALWAYS
                        titleProperty.set(messages["keepOriginal"])
                        subTitleProperty.set(messages["keepOriginalDescription"])

                        setOnAction {
                            conflictResolutionProperty.set(ConflictResolution.DISCARD)
                        }
                        prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                    }

                    cardRadioButton(tg) {
                        hgrow = Priority.ALWAYS
                        prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                        titleProperty.set(messages["overrideWithNew"])
                        subTitleProperty.set(messages["overrideWithNewDescription"])

                        setOnAction {
                            conflictResolutionProperty.set(ConflictResolution.OVERRIDE)
                        }
                        isSelected = true
                    }
                }
            }

            hbox {
                addClass("confirm-dialog__footer")
                region { hgrow = Priority.ALWAYS }
                button(messages["cancelImport"]) {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                    onActionProperty().bind(onCloseActionProperty)
                }
                button(messages["submit"]) {
                    addClass("btn", "btn--primary")
                    graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)
                    onActionProperty().bind(onSubmitActionProperty)
                }
            }
        }

    init {
        setContent(content)
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }

    fun setOnSubmitAction(op: (ConflictResolution) -> Unit) {
        onSubmitActionProperty.set(EventHandler { op(conflictResolutionProperty.value) })
    }
}
