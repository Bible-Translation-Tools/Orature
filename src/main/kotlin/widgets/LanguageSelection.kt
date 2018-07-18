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
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import recources.UIColors
import tornadofx.*
import widgets.LanguageSelectionStyle.Companion.makeItHoverBLUE
import widgets.LanguageSelectionStyle.Companion.makeItHoverRED


/**
 * This class is used to make the drop-downs for adding new
 * target or source languages and adds highlightable and
 * deletable buttons for each language selected.
 *
 *
 *   -------- PLEASE NOTE ---------
 * This will eventually need a reference to the Profile variable
 * observables once it is made ready in the common component.
 *
 * KNOWN BUGS:
 * >>  Deleting / using enter / clicking will keep / place the language related
 *     language in the ComboBox text field
 * >>  Current implementation requires manual input of colors and not just a stylesheet
 *     (not really a bug, but needs to be fixed)
 * >>  Caleb and Kimberly worked on this, so it's just a given that there are more bugs
 *     and even more poor function- and variable-naming practices
 *
 *   --------- PARAMETERS ---------
 * @list - observable list of strings (languages)
 * @input - the user's selected string
 * @label - title to go above the ComboBox
 * @colorAccent - the accented color for selected buttons
 * @colorNeutral - the color for non-selected buttons (and text in selected button)
 * @colorNeutralTest - color for text inside non-selected buttons
 * @hint - string for hint text inside the combobox
 * @styleClass - cssRule styling class
 * @selectedLanguages - an observable list of strings to contain the user's selected options
 */
class LanguageSelection(list : ObservableList<String>,
                        input : SimpleStringProperty,
                        label : String,
                        colorAccent : Color,
                        colorNeutral : Color,
                        colorNeutralText : Color,
                        hint : String,
                        styleClass : CssRule,
                        private val selectedLanguages : ObservableList<String>
) : Fragment() {

    // declare these as private variables because for some reason the parameters aren't accessible by themselves
    /* my (kimberly's) prof will be really disappointed
    ** that I didn't put underscores in front of the private var names */
    private val colorAccent = colorAccent // hopefully using the same var name here doesn't destroy anything
    private val colorNeutral = colorNeutral
    private val colorNeutralText = colorNeutralText

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

            // TODO: Find out how to shorten lambda syntax
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

    /**
     * Adds the buttons / toggles to the screen
     */
    private fun addTagNode(language : String) : Node {

        // creates background for the tag
        val background = Rectangle()
        background.fill = colorAccent
            // will need to make the dimensions dynamic (maybe)
        background.arcWidth = 30.0
        background.arcHeight = 30.0
        background.width = 100.0
        background.height = 25.0

        // dynamic padding?
        val label = Label(language)
        val labelHBox = HBox(label)
        labelHBox.alignment = Pos.CENTER_LEFT
        labelHBox.padding = Insets(20.0)
        label.textFill = colorNeutral

        val deleteButton = Button("X")
        deleteButton.userData = language // pro tip (thanks Carl)
        deleteButton.textFill = colorNeutral
        deleteButton.action {
            selectedLanguages.remove( deleteButton.userData as String )
            if (background.fill == colorAccent && selectedLanguages.isNotEmpty()) {
                resetSelected()
            }
        }

        val labelDelB = HBox(deleteButton)
        labelDelB.alignment = Pos.CENTER_RIGHT
        labelDelB.padding = Insets(10.0)

        val sp = StackPane(background, labelHBox, labelDelB)

        // added class to add hover effect, color changes depending on label
        // this is very hard-coded and should be changed
        // TODO: change this nasty hard-coding yikes
        if(colorAccent == Color.valueOf(UIColors.UI_PRIMARY)) {
            sp.addClass(makeItHoverRED)
        } else {
            sp.addClass(makeItHoverBLUE)
        }

        // dynamic scaling needed
        sp.prefHeight = 25.0
        sp.prefWidth = 100.0
        if (fp.children.isNotEmpty()) {
            newSelected(language)
        }

        // Find out how to shorten lambda syntax
        sp.addEventFilter(MouseEvent.MOUSE_CLICKED, EventHandler<MouseEvent> { mouseEvent -> newSelected(language)})
        return sp
    }

    /**
     * If a selected tag is removed, the new selected tag will be
     * the most recent one added
     */
    private fun resetSelected() {
        // get first button in the list
        val firstTag = fp.children[0]
        for (nodeOut in firstTag.getChildList().orEmpty()) {
            // make the rectangle the accent color
            if (nodeOut is Rectangle) {
                nodeOut.fill = colorAccent
            } else if (nodeOut is HBox) {
                for (nodeIn in nodeOut.children) {
                    // set label and button text to white
                    if (nodeIn is Label) {
                        nodeIn.textFill = colorNeutral
                    } else if (nodeIn is Button) {
                        nodeIn.textFill = colorNeutral
                    }
                }
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
                // and kek* a reference
                    // *originally keep but the typo was too good
                if (nodeOut is Rectangle) {
                    rectangleReference = nodeOut
                    nodeOut.fill = colorNeutral
                } else if (nodeOut is HBox) {
                    // find the label and check if it equals the selected label
                    for (nodeIn in nodeOut.children) {
                        // if so, then highlight the rectangle color and change the text color
                        if (nodeIn is Label) {
                            if (nodeIn.text == tag) {
                                rectangleReference.fill = colorAccent
                                nodeIn.textFill = colorNeutral
                            }
                            // otherwise, make it the non-selected color with neutral-colored text
                            else {
                                rectangleReference.fill = colorNeutral
                                nodeIn.textFill = colorNeutralText
                            }
                            // and also change the button color according to the rectangle fill
                        } else if (nodeIn is Button) {
                            if (rectangleReference.fill == colorNeutral) {
                                nodeIn.textFill = colorNeutralText
                            } else {
                                nodeIn.textFill = colorNeutral
                            }
                        }
                    }
                }
            }
        }
    }
}