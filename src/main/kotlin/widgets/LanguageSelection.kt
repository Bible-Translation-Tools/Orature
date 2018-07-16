package widgets

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import recources.UIColors
import tornadofx.*


/**
 * This class is used to make the drop-downs for adding new
 * target or source languages and adds highlightable and
 * deletable buttons for each language selected
 *
 *
 *   -------- PLEASE NOTE ---------
 * This will eventually need a reference to the Profile variable
 * observables once it is made ready in the common component
 *
 * KNOWN BUGS:
 * Deleting / using enter / clicking will keep / place the language related
 * language in the ComboBox text field
 */

class LanguageSelection(list : ObservableList<String>,
                        input : SimpleStringProperty,
                        label : String,
                        hint : String,
                        styleClass : CssRule,
                        private val selectedLanguages : ObservableList<String>
) : Fragment() {

    private val fp = flowpane {

        children.bind(selectedLanguages, ::addTagNode)

        vgrow = Priority.ALWAYS

        hgap = 6.0
        vgap = 6.0
    }

    override val root = vbox {

        alignment = Pos.CENTER

        label(label)

        combobox(input, list) {


            /**
             * Give it some personal space
             */
            vboxConstraints {
                marginLeft = 10.0
                marginTop = 10.0
                marginRight = 10.0
                marginBottom = 10.0
            }

            addClass(styleClass)

            /**
             * Allow filtered searching
             */
            isEditable = true
            makeAutocompletable(false)

            promptText = hint

            // Find out how to shorten lambda syntax
            addEventFilter(ComboBox.ON_HIDDEN, {
                if( input.isNotEmpty.value && list.contains(input.value) && !selectedLanguages.contains(input.value) ) {
                    selectedLanguages.add(0, input.value )
                }
            })

        }

        separator {}

        add(fp) // should find better solution, but reference to flowplane is needed outside of root

        padding = Insets(40.0)
        spacing = 10.0

    }

    private fun addTagNode(language : String) : Node {

        val background = Rectangle()
        background.fill = Paint.valueOf(UIColors.UI_PRIMARY)
        // will need to make the dimensions dynamic
        background.arcWidth = 20.0
        background.arcHeight = 20.0
        background.width = 200.0
        background.height = 40.0
        background.effect = DropShadow(3.0, Color.DARKGRAY)

        // dynamic padding?
        val label = Label(language)
        val labelHBox = HBox(label)
        labelHBox.alignment = Pos.CENTER_LEFT
        labelHBox.padding = Insets(20.0)

        val deleteButton = Button("X")
        deleteButton.userData = language // pro tip (thanks Carl)
        deleteButton.action {
            selectedLanguages.remove( deleteButton.userData as String )
            if (background.fill == Paint.valueOf(UIColors.UI_PRIMARY) && selectedLanguages.isNotEmpty()) {
                resetSelected()
            }
        }

        val labelDelB = HBox(deleteButton)
        labelDelB.alignment = Pos.CENTER_RIGHT
        labelDelB.padding = Insets(10.0)

        val sp = StackPane(background, labelHBox, labelDelB)
        // dynamic scaling needed
        sp.prefHeight = 40.0
        sp.prefWidth = 200.0
        if (fp.children.isNotEmpty()) {
            newSelected(language)
        }

        // Find out how to shorten lambda syntax
        sp.addEventFilter(MouseEvent.MOUSE_CLICKED, EventHandler<MouseEvent> { mouseEvent -> newSelected(language)})
        return sp
    }

    /**
     * If a selected tag is removed, the new selected tag will be
     * the most recently one added
     */
    private fun resetSelected() {
        val firstTag = fp.children[0]
        for (nodeOut in firstTag.getChildList().orEmpty()) {
            if (nodeOut is Rectangle) {
                nodeOut.fill = Paint.valueOf(UIColors.UI_PRIMARY)
            }
        }

    }

    /**
     * Change the highlighted tag to the one most recently clicked
     */
    private fun newSelected(tag : String) {

        val elements = fp.children
        var rectangleReference = Rectangle()

        // for all objects in the flowpane
        for (children in elements) {

            // for all children in each flowplane object
            for (nodeOut in children.getChildList().orEmpty()) {

                // change the rectangle color
                // and kep a reference
                if (nodeOut is Rectangle) {
                    rectangleReference = nodeOut
                    nodeOut.fill = Color.WHITE
                } else if (nodeOut is HBox) {

                    // find the label and check if it equals the selected label
                    for (nodeIn in nodeOut.children) {

                        // if so, then highlight the rectangle color
                        if (nodeIn is Label) {
                            if (nodeIn.text == tag) {
                                rectangleReference.fill = Paint.valueOf(UIColors.UI_PRIMARY)
                            }
                        }
                    }
                }
            }
        }
    }

}