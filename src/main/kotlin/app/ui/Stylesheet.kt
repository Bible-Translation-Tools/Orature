package app.ui

import com.jfoenix.controls.JFXProgressBar
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import tornadofx.*

class ChartCSS : Stylesheet() {
    companion object {

        val transparentButton by cssclass()

        val bg by cssproperty<MultiValue<Paint>>("-fx-background-color")
    }
    init {
        s(label) {
            font = Font.font("NotoSans-Black")
        }

        s(button) {
            bg.value += c("#E56060")
            textFill = Color.WHITE
        }

        transparentButton {
            bg.value += Color.TRANSPARENT
        }

        s(bar, track) {
            backgroundRadius += box(50.px)
        }
        s(track) {
            backgroundColor += Color.TRANSPARENT
        }
    }
}