package org.wycliffeassociates.otter.jvm.controls.highlightablebutton

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.text.FontWeight
import tornadofx.*

class HighlightableButtonStyles : Stylesheet() {

    companion object {
        val hButton by cssclass()
    }

    init {
        hButton {
            alignment = Pos.CENTER
            maxHeight = 40.px
            borderRadius += box(5.0.px)
            backgroundRadius += box(5.0.px)
            cursor = Cursor.HAND
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }
    }
}
