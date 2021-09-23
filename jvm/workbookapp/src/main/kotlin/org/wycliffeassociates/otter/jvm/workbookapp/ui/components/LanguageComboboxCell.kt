package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language

class LanguageComboboxCell : ListCell<Language>() {
    val view = ComboboxButton()
    override fun updateItem(item: Language?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = view.apply {
            textProperty.set(item.name)
            iconProperty.set(FontIcon(MaterialDesign.MDI_WEB))
        }
    }
}
