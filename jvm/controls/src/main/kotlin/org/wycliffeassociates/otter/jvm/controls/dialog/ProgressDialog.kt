package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ProgressDialog : OtterDialog() {
    
    val dialogTitleProperty = SimpleStringProperty()
    val dialogMessageProperty = SimpleStringProperty()
    val percentageProperty = SimpleDoubleProperty(0.0)
    val progressMessageProperty = SimpleStringProperty()
    val cancelMessageProperty = SimpleStringProperty()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    
    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        hbox {
            addClass("confirm-dialog__header")
            label {
                textProperty().bind(dialogTitleProperty)
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
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label {
                addClass("normal-text")
                textProperty().bind(dialogMessageProperty)
            }
            progressbar {
                progressProperty().bind(percentageProperty.divide(100))
                fitToParentWidth()
            }
            hbox {
                label {
                    addClass("h5")
                    textProperty().bind(progressMessageProperty.stringBinding { it?.let { messages[it] } ?: "" })
                }
                region { hgrow = Priority.ALWAYS }
                label {
                    addClass("normal-text")
                    textProperty().bind(percentageProperty.stringBinding {
                        String.format("%.0f%%", it ?: 0.0)
                    })
                }
            }
        }
        hbox {
            addClass("confirm-dialog__footer")
            region { hgrow = Priority.ALWAYS }
            button(cancelMessageProperty) {
                addClass("btn", "btn--secondary")
                onActionProperty().bind(onCloseActionProperty)
            }
            visibleWhen { cancelMessageProperty.isNotNull }
            managedWhen(visibleProperty())
        }
    }

    init {
        setContent(content)
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }
}

fun EventTarget.progressDialog(op: ProgressDialog.() -> Unit) = ProgressDialog().apply(op)