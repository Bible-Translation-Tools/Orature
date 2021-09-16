package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import javax.sound.sampled.Mixer

class DeviceComboboxCell : ListCell<Mixer.Info>() {
    val view = ComboboxButton()
    override fun updateItem(item: Mixer.Info?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = view.apply {
            textProperty.set(item.name)
            iconProperty.set(FontIcon(MaterialDesign.MDI_VOLUME_HIGH))
        }
    }
}
