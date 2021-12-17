package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import javax.sound.sampled.Mixer

class DeviceComboboxCell(private val icon: FontIcon) : ListCell<String>() {
    val view = ComboboxButton()
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = view.apply {
            textProperty.set(item)
            iconProperty.set(icon)
        }
    }
}
