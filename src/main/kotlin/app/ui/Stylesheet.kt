package app.ui

import com.jfoenix.controls.JFXProgressBar
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class ChartCSS : Stylesheet() {
    companion object {
        val defaultPieChart by cssclass()
        val progressBarStyle by cssclass()

        val bg by cssproperty<MultiValue<Paint>>("-fx-background-color")
    }
    init {
        s(button) {
            bg.value += Color.LEMONCHIFFON
        }

        /**
         * Styles the progress bar.
         */
        progressBarStyle {
            alignment = Pos.TOP_CENTER
            backgroundRadius += box(50.px)
            minWidth = 150.px
            accentColor = c("#E56060")
        }
        s(bar, track) {
            backgroundRadius += box(50.px)
        }
        s(track) {
            backgroundColor += Color.TRANSPARENT
        }
    }
}