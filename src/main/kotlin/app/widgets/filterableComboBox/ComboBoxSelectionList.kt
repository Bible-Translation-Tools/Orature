package app.widgets.filterableComboBox

import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * This class takes in a list of ComboBoxSelectionItems and adds each one's labelText to an observable list.
 *
 * @author Caleb Benedick
 *
 * @param dataList A list of ComboBoxSelectionItems whose labelText properties will be mapped to an observable list
 */
class ComboBoxSelectionList(val dataList : List<ComboBoxSelectionItem>) {
    val observableList : ObservableList<String> = FXCollections.observableList(mutableListOf())

    init {
        observableList.addAll(dataList.map { it.labelText })
    }
}
