package widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import tornadofx.*

/**
 * A chip is a StackPane object containing a label, a delete button, and a rectangle (for the background);
 * it can be clicked or deleted, and functions are passed in to define what is done on clicking or deleting.
 *
 * @author Caleb Benedick and Kimberly Horton
 *
 * @param mainText The main text to be displayed on the chip.
 * @param subText The text to be displayed within parentheses on the chip
 * @param onDelete Function that details what should be done when the delete button is clicked.
 * @param onClick Function that details what should be done when the chip is clicked.
 */
class Chip(
        val mainText: String,
        val subText: String,
        onDelete : (String) -> Unit,
        onClick : (String) -> Unit
) : StackPane() {

    val mainLabel : Label
    val subLabel : Label
    val deleteButton : Button
    val button : Rectangle

    init {

        mainLabel = label(mainText) { id = "mainLabel" }

        subLabel = label("($subText)") { id = "subLabel" }
        subLabel.textFillProperty().bind(mainLabel.textFillProperty())

        deleteButton = button {
            val deleteIcon = MaterialIconView(MaterialIcon.CLEAR, "20px")
            deleteIcon.fillProperty().bind(mainLabel.textFillProperty())
            add(deleteIcon)
            action {
                onDelete(mainText)
            }
        }

        button = rectangle {
            height = 25.0

            // bind the width to the size of the text in the label
            widthProperty().bind(mainLabel.widthProperty() + subLabel.widthProperty()
                    + deleteButton.widthProperty())
        }

        add(button)
        add(HBox(mainLabel, subLabel, deleteButton))

        addEventFilter(MouseEvent.MOUSE_CLICKED) { onClick(mainText) }
    }

}
