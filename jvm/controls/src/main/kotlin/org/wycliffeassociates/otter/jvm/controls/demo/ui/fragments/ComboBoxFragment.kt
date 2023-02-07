package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.combobox.ComboboxItem
import org.wycliffeassociates.otter.jvm.controls.combobox.IconComboBoxCell
import tornadofx.*

class ComboBoxFragment : Fragment() {
    private val selected = SimpleStringProperty("Item1")
    private val items = FXCollections.observableArrayList("Item1", "Item2", "Item3")

    override val root = stackpane {
        combobox(selected, items) {
            addClass("wa-combobox")

            cellFormat {
                val view = ComboboxItem()
                graphic = view.apply {
                    topTextProperty.set(it)
                }
            }
            buttonCell = IconComboBoxCell(FontIcon(MaterialDesign.MDI_ACCOUNT))
        }
    }
}