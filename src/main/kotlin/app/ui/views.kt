package app.ui

import app.ui.ChartCSS.Companion.progressBarStyle
import app.ui.ChartCSS.Companion.transparentButton
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*
import kotlin.concurrent.thread
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * sweet code for a heckin cool loading bar
 */
class ProgressBar : View() {
    override val root = vbox {
        var isItDone = false

        style {
            alignment = Pos.TOP_CENTER
        }

        progressbar {
            addClass(progressBarStyle)
            thread {
                while (!isItDone) {
                    for (i in 1..100) {
                        Platform.runLater { progress = i.toDouble() / 100.0 }
                        Thread.sleep(10)
                    }
                }
            }
        }
        button("Take the Upgrade") { // oof this text is a reference to a musical, ignore me
            action {
                isItDone = true
                println(isItDone)
            }
        }
    }
}

/**
 * sweet code for making heckin buttons/chips of each language selected
 */
class languageSelector : Fragment() {
    var languageList = mutableListOf<String>().observable()

    override val root = vbox {
        var language = SimpleStringProperty()

        /* this textfield doesn't clear, but that's bc i'm assuming
         * caleb has an auto-clearing feature in his combobox. */
        textfield(language)
        button("Submit") {
            action {
                if( language.isNotEmpty().value && !languageList.contains(language.value) ) {
                    languageList.add( language.value )
                }
            }
        }

        /**
         * FYI from this point on a lot of this is carl's code with my comments.
         * I pretty much fully understand it all (so it's not blind copy/paste)
         * but I want to clarify that the structure is not completely my own.
         * Stuff I don't understand is marked, and I'm researching it.
         */
        separator()
        flowpane {

            // binds the flowpane children to selectedTags
            children.bind(languageList, ::addTagNode)

            vgrow = Priority.ALWAYS // what is vgrow?

            hgap = 6.0 // what are these?
            vgap = 6.0 // ????
        }
        padding = Insets(40.0)
        spacing = 10.0
    }

    fun addTagNode(langString : String) : Node {

        // creates the background for the tag
        val background = Rectangle()
        background.style {
            fill = Color.WHITE
            effect = DropShadow()
        }
            background.arcWidth = 20.0
            background.arcHeight = 20.0
            background.width = 100.0
            background.height = 25.0

        // creates the label/text inside the tag
        val label = Label(langString)
        val labelHBox = HBox(label)
        labelHBox.alignment = Pos.CENTER_LEFT
        labelHBox.padding = Insets(20.0)

        // deletes button; when clicked it removes the data
        val delete_b = Button("X")
        delete_b.userData = langString
        delete_b.action {
            languageList.remove( delete_b.userData as String )
        }
        delete_b.addClass(transparentButton)

        val labelDelB = HBox(delete_b)
        labelDelB.alignment = Pos.CENTER_RIGHT
        labelDelB.padding = Insets(10.0)

        // stack the background, label, and delete button and then return it all as a thing
        val chip = StackPane(background, labelHBox, labelDelB)
        chip.prefHeight = 25.0
        chip.prefWidth = 100.0

        return chip
    }
}