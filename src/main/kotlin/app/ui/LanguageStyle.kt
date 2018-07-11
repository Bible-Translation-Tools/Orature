package app.ui

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

/**
 * This class is the style sheet for the language search
 * drop-downs for target and source languages
 *
 * Each is specified by it's color
 */
class LanguageStyle : Stylesheet() {

    companion object {
        val targetLanguageSelector by cssclass()
        val sourceLanguageSelector by cssclass()
    }

    private val targetColor = Paint.valueOf("#e56060")
    private val sourceColor = Paint.valueOf("#3db5ff")

    init {

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