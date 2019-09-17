package org.wycliffeassociates.otter.jvm.app.widgets.progressdialog

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class ProgressDialogStyles : Stylesheet() {
    companion object {
        val defaultProgressDialog by cssclass()
        val progressGraphic by cssclass()
    }

    init {
        defaultProgressDialog {
            // Graphic
            progressIndicator {
                maxWidth = 125.px
                maxHeight = 125.px
            }
            prefWidth = 500.px
            prefHeight = 300.px

            label {
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
                padding = box(20.px, 20.px)
                textAlignment = TextAlignment.CENTER
                fillWidth = true
                maxWidth = Double.MAX_VALUE.px
                alignment = Pos.CENTER
                wrapText = true
            }
        }
    }
}