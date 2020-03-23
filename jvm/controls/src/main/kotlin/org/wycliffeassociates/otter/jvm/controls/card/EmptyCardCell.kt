package org.wycliffeassociates.otter.jvm.controls.card

import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import tornadofx.*

class EmptyCardCell : Rectangle() {
    init {
        arcHeight = 25.0
        arcWidth = 25.0

        fill = Paint.valueOf("#DDDDDD")

        style {
            borderRadius += box(25.px)
            backgroundRadius += box(25.px)
        }
    }
}