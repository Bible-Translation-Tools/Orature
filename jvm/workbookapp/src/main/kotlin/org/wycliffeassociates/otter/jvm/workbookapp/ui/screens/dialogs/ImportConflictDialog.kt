package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import tornadofx.*

class ImportConflictDialog : OtterDialog() {

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onSubmitActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val conflictResolutionProperty = SimpleObjectProperty<ConflictResolution>(ConflictResolution.DISCARD)

    private val content = VBox().apply {
        addClass("confirm-dialog")

        hbox {
            label("[Book Name] Import")
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                onActionProperty().bind(onCloseActionProperty)
            }
        }

        vbox {
            label("There was an issue with ...")
            hbox {
                val tg = ToggleGroup()
                radiobutton("Keep Original") {
                    toggleGroup = tg
                    isSelected = true
                    addClass("wa-radio")
                    action {
                        conflictResolutionProperty.set(ConflictResolution.DISCARD)
                    }
                }
                radiobutton("Override with New") {
                    toggleGroup = tg
                    addClass("wa-radio")
                    action {
                        conflictResolutionProperty.set(ConflictResolution.OVERRIDE)
                    }
                }
            }
        }

        hbox {
            button(messages["cancelImport"]) {
                onActionProperty().bind(onCloseActionProperty)
            }
            button(messages["submit"]) {
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

enum class ConflictResolution {
    DISCARD,
    OVERRIDE
}