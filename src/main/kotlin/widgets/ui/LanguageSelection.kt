package widgets.ui

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.paint.Paint
import tornadofx.*


/**
 * This class is used to make the drop-downs for adding new
 * target or source languages
 */

class LanguageSelection(list : ObservableList<String>,
                              input : SimpleStringProperty,
                              label : String,
                              hint : String,
                              styleClass : CssRule
) : Fragment() {

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
            makeAutocompletable()

            promptText = hint

        }

    }
}