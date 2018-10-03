package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

import javafx.application.Platform
import javafx.scene.control.ComboBox
import javafx.scene.paint.Color
import tornadofx.*

/**
 * This class contains a comboBox that is searchable and filterable through a text field. It selects and passes an item
 * if a valid on is in the text field when the drop box closes and auto selects any text in the field when refocusing
 * back on the comboBox
 *
 * @author Caleb Benedick
 *
 * @param selectionData The list of ComboBoxSelectionItems to be selected from in the comboBox
 * @param hint The display text in an empty comboBox text field
 * @param onComboBoxHidden The function to call when the comboBox drop down is closed and a valid item is in the text
 * field
 */
class FilterableComboBox (
        selectionData: List<ComboBoxSelectionItem>,
        hint: String,
        onComboBoxHidden : (ComboBoxSelectionItem) -> Unit
) : ComboBox<String>() {

    init {
        val comboBoxSelectionList = ComboBoxSelectionList(selectionData)
        items = comboBoxSelectionList.observableList

        /** Set up filterable comboBox based on the incoming data to select from */
        isEditable = true
        promptText = hint
        makeAutocompletable(false) {
            comboBoxSelectionList.dataList.filter { current ->
                current.filterText.contains(it) ||
                        current.labelText.contains(it, true)
            }.map { it.labelText }.sorted()
        }

        editor.style {
            backgroundColor = multi(Color.TRANSPARENT)
        }

        /** Select any text in the editor when it is refocused */
        editor.focusedProperty().addListener { _, _, _ ->
            run {
                Platform.runLater {
                    if (editor.isFocused && !editor.text.isEmpty()) {
                        editor.selectAll()
                    }
                }
            }
        }

        /** Set the comboBox selected value to the value in the text editor */
        editor.textProperty().addListener { _, _, newText -> value = newText }

        /** Add selected item if valid when the comboBox dropdown closes */
        addEventFilter(ComboBox.ON_HIDDEN) {
            if (comboBoxSelectionList.observableList.contains(value)) {
                val index = comboBoxSelectionList.observableList.indexOf(value)
                onComboBoxHidden(comboBoxSelectionList.dataList[index])
            }
        }
    }
}
