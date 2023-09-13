package org.wycliffeassociates.otter.jvm.controls.dialog

import com.jfoenix.controls.JFXProgressBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class LoadingModal : OtterDialog() {

    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label(messages["loadingProjectWait"]) {
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