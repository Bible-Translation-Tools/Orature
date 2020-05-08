package org.wycliffeassociates.otter.jvm.controls.sourcedialog

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.wycliffeassociates.otter.jvm.controls.styles.AppTheme
import tornadofx.*

class SourceDialogStyles : Stylesheet() {
    companion object {
        val defaultSourceDialog by cssclass()
        val message by cssclass()
        val closeButton by cssclass()
    }

    init {
        defaultSourceDialog {
            prefWidth = 700.px

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

            message {
                fontWeight = FontWeight.NORMAL
                padding = box(0.px, 50.px, 30.px, 50.px)
            }

            closeButton {
                minHeight = 40.px
                minWidth = 187.0.px
                backgroundColor += AppTheme.colors.white
                borderColor += box(AppTheme.colors.appBlue)
                borderWidth += box(2.px)
                borderRadius += box(5.px)
                backgroundRadius += box(5.px)
                child("*") {
                    fill = AppTheme.colors.appBlue
                }
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }
    }
}
