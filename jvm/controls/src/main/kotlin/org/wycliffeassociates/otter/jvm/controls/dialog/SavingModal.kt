package org.wycliffeassociates.otter.jvm.controls.dialog

import io.github.palexdev.materialfx.controls.MFXProgressBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class SavingModal : OtterDialog() {
    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label(messages["savingProjectWait"]) {
                addClass("confirm-dialog__message", "normal-text")
            }
            add(
                MFXProgressBar().apply {
                    prefWidthProperty().bind(this@vbox.widthProperty())
                }
            )
        }

    }

    init {
        setContent(content)
    }
}




