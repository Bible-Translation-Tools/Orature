package widgets

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import tornadofx.*


/**
 * Bellow main and app are used to demo
 */

fun main(args: Array<String>) {
    println("hello")

    launch<LanguagesDemoApp>(args)
}

class LanguagesDemoApp : App(MainView::class, LanguageSelectionStyle::class)

/**
 * Above main and app are used to demo
 */


class MainView : View() {

    private val newTarget = SimpleStringProperty()
    private val newSource = SimpleStringProperty()

    private val selectedTargets = mutableListOf<String>().observable()
    private val selectedSources = mutableListOf<String>().observable()


    private val languages = listOf("English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew", "English", "Spanish", "French", "Russian", "Engrish", "Sppanish", "Arabic", "MandArin", "Afrikaans", "Hebrew")

    private val hint = "Try English"

    override val root = hbox {

        setPrefSize(800.0, 400.0)
        alignment = Pos.CENTER

        // Target Language ComboBox
        add(LanguageSelection(
                FXCollections.observableList(languages),
                newTarget,
                "Target Languages",
                hint,
                LanguageSelectionStyle.targetLanguageSelector,
                selectedTargets
        ))

        // Source Language ComboBox
        add(LanguageSelection(
                FXCollections.observableList(languages),
                newSource,
                "Source Languages",
                hint,
                LanguageSelectionStyle.sourceLanguageSelector,
                selectedSources
        ))

    }

}