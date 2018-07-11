package app.ui

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.paint.Paint
import tornadofx.*


/**
 * Bellow main and app are used to demo
 */

fun main(args: Array<String>) {
    println("hello")

    launch<LanguagesDemoApp>(args)
}

class LanguagesDemoApp : App(MainView::class, LanguageStyle::class)

/**
 * Above main and app are used to demo
 */


class MainView : View() {

    private val newTarget = SimpleStringProperty()
    private val newSource = SimpleStringProperty()

    private val languages = listOf("English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew")

    private val hint = "Try English"

    private val targetColor = Paint.valueOf("#e56060")
    private val sourceColor = Paint.valueOf("#3db5ff")

    private val targetBox = LanguageSelectionWidget(
            FXCollections.observableList(languages),
            newTarget,
            "Target Languages",
            hint,
            LanguageStyle.targetLanguageSelector
    )

    private val sourceBox = LanguageSelectionWidget(
            FXCollections.observableList(languages),
            newSource,
            "Source Languages",
            hint,
            LanguageStyle.sourceLanguageSelector
    )

    override val root = hbox {

        setPrefSize(400.0, 200.0)
        alignment = Pos.CENTER
        add(targetBox)
        add(sourceBox)

    }
}