package org.wycliffeassociates.otter.jvm.controls.dialog

import com.jfoenix.controls.JFXProgressBar
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class LoadingModal : OtterDialog() {

    val messageProperty = SimpleStringProperty()

    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label(messageProperty) {
                addClass("confirm-dialog__message", "normal-text")
            }
            add(
                JFXProgressBar().apply {
                    prefWidthProperty().bind(this@vbox.widthProperty())
                }
            )
        }

    }

    init {
        setContent(content)
    }
}