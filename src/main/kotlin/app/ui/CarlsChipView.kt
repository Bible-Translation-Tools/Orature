package app.ui

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*


/**
 * CARL MY MAN DID AN AMAZING THING, WHAT A BLESSED DAY
 * ALL HAIL CARL WALKER, THE MAN, THE MYTH, THE LEGEND
 */
class TagCloudView : View("Tag Cloud") {

    val tags = listOf(
            "Engrish", "ManArin", "Hebrew", "Aramaic", "Hebrew", "Hebrew", "Icelandic"
    ).observable()

    val tagToAdd = SimpleStringProperty()
    val selectedTags = mutableListOf<String>().observable()

    override val root = vbox {

        combobox<String> {
            tagToAdd.bind( selectionModel.selectedItemProperty() )
            items = tags
        }
        button("Add") {
            action {
                if( tagToAdd.isNotEmpty().value && !selectedTags.contains(tagToAdd.value) ) {
                    selectedTags.add( tagToAdd.value )
                }
            }
        }
        separator()
        flowpane {

            // binds the flowpane children to selectedTags
            children.bind(selectedTags, ::addTagNode)

            vgrow = Priority.ALWAYS

            hgap = 6.0
            vgap = 6.0
        }

        padding = Insets(40.0)
        spacing = 10.0

    }

    fun addTagNode(s : String) : Node {

        // creates the background for the tag
        val background = Rectangle()
        background.fill = Color.WHITE
        background.arcWidth = 20.0
        background.arcHeight = 20.0
        background.width = 200.0
        background.height = 40.0
        background.effect = DropShadow()

        // creates the label inside the tag?
        val label = Label(s)
        val labelHBox = HBox(label)
        labelHBox.alignment = Pos.CENTER_LEFT
        labelHBox.padding = Insets(20.0)

        // deletes button; when clicked it removes the data
        val delete_b = Button("X")
        delete_b.userData = s // pro tip      <-- this comment is from carl
        delete_b.action {
            selectedTags.remove( delete_b.userData as String )
        }

        val labelDelB = HBox(delete_b)
        labelDelB.alignment = Pos.CENTER_RIGHT
        labelDelB.padding = Insets(10.0)

        // stack the background, label, and delete button and then return it all as a thing
        val sp = StackPane(background, labelHBox, labelDelB)
        sp.prefHeight = 40.0
        sp.prefWidth = 200.0

        return sp
    }
}