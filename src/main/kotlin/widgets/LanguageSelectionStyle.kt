package widgets

import com.sun.javafx.geom.Rectangle
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import recources.UIColors
import tornadofx.*

/**
 * This class is the style sheet for the language search
 * drop-downs for target and source languages
 *
 * Each is specified by it's color
 */

class LanguageSelectionStyle : Stylesheet() {

    companion object {
        val targetLanguageSelector by cssclass()
        val sourceLanguageSelector by cssclass()
        val makeItHoverRED by cssclass()
        val makeItHoverBLUE by cssclass()
        val bg by cssproperty<MultiValue<Paint>>("-fx-background-color")
    }

    private val targetColor = Paint.valueOf(UIColors.UI_PRIMARY)
    private val sourceColor = Paint.valueOf(UIColors.UI_SECINDARY)
    private val notoFont = Font.font("NotoSans-Black", 8.0)

    init {
        s(button) {
            bg.value += Color.TRANSPARENT
        }

        s(label) {
            font = notoFont
            fontSize = 10.pt
        }

        makeItHoverRED {
            and(hover) {
                effect =  DropShadow(5.0, Color.valueOf(UIColors.UI_PRIMARY))
            }
        }
        makeItHoverBLUE {
            and(hover) {
                effect =  DropShadow(5.0, Color.valueOf(UIColors.UI_SECINDARY))
            }
        }


        targetLanguageSelector {

            borderColor = multi(box(targetColor))
            focusColor = targetColor
            faintFocusColor = Color.TRANSPARENT

            s(arrowButton) {
                backgroundColor = multi(Color.TRANSPARENT)
            }

            s(listView) {
                maxHeight = 125.px
            }

        }

        sourceLanguageSelector {

            borderColor = multi(box(sourceColor))
            focusColor = sourceColor
            faintFocusColor = Color.TRANSPARENT

            s(arrowButton) {
                backgroundColor = multi(Color.TRANSPARENT)
            }

            s(listView) {
                maxHeight = 125.px
            }
        }

    }
}