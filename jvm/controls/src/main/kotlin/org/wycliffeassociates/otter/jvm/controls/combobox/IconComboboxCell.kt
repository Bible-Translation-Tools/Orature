package org.wycliffeassociates.otter.jvm.controls.combobox

import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon

class IconComboBoxCell<T>(
    private val icon: FontIcon,
    private val convertText: ((item: T) -> String)? = null
) : ListCell<T>() {
    val view = ComboboxButton()
    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = view.apply {
            val text = convertText?.let { it(item) } ?: item.toString()
            textProperty.set(text)
            iconProperty.set(icon)
        }
    }
}