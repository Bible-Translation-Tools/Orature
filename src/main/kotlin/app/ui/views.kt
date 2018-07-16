package app.ui

/**
 * THIS IS A LOT OF IMPORTS HECC
 */
import app.ui.ChartCSS.Companion.transparentButton
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.util.*

/**
 * sweet code for making heckin buttons/chips of each language selected
 */
class languageSelector : Fragment() {
    var languageList = mutableListOf<String>().observable()

    init {// pulls correct language
        messages = ResourceBundle.getBundle("MyView")}

    override val root = vbox {
        var language = SimpleStringProperty()

        /* this textfield doesn't clear, but that's bc i'm assuming
         * caleb has an auto-clearing feature in his combobox. */
        textfield(language)
        button(messages["submitButton"]) {
            action {
                if( language.isNotEmpty().value && !languageList.contains(language.value) ) {
                    languageList.add( language.value )
                }
            }
        }

        /**
         * Code structure and content adapted from Carl Walker
         */
        separator()
        flowpane {

            // binds the flowpane children to selectedTags
            children.bind(languageList, ::addChipNode)

            vgrow = Priority.ALWAYS // what is vgrow?

            hgap = 6.0 // what are these?
            vgap = 6.0 // ????
        }
        padding = Insets(40.0)
        spacing = 10.0
    }

    /**
     * Pulls together the language
     */
    fun addChipNode(langString : String) : Node {

        // creates the background for the tag
        val background = Rectangle()
        background.style {
            fill = c("#E56060")
            effect = DropShadow()
        }
            background.arcWidth = 20.0
            background.arcHeight = 20.0
            background.width = 100.0
            background.height = 25.0

        // creates the label/text inside the tag
        /**
         * GOAL: Make this a button that changes the color and 'default' status when clicked.
         * You might need to make a "language" object as a model and pass that in instead of
         * a string/SimpleStringProperty.
         */
        val labelButton = Button(langString)
        val label = HBox(labelButton)
        label.alignment = Pos.CENTER_LEFT
        label.padding = Insets(20.0)
        labelButton.addClass(transparentButton)
        labelButton.style {
            textFill = Color.WHITE
        }
        labelButton.action {
            println("henlo")
            background.fill = Color.WHITE
        }

        // deletes button; when clicked it removes the data
        val delete_b = Button("X")
        delete_b.userData = langString
        delete_b.action {
            languageList.remove(delete_b.userData as String)
        }
        //delete_b.addClass(transparentButton)

        val labelDelB = HBox(delete_b)
        labelDelB.alignment = Pos.CENTER_RIGHT
        labelDelB.padding = Insets(10.0)

        // stack the background, label, and delete button and then return it all as a thing
        val chip = StackPane(background, labelDelB, labelButton)
        //chip.prefHeight = 25.0
        //chip.prefWidth = 100.0
        //chip.padding = Insets(20.0)

        return chip
    }
}