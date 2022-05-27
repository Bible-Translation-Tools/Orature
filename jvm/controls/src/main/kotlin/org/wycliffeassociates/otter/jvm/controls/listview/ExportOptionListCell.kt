package org.wycliffeassociates.otter.jvm.controls.listview

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import tornadofx.*
import tornadofx.FX.Companion.messages

class ExportOptionListCell : ListCell<ExportOption>() {
    private fun associatedIcon(option: ExportOption): FontIcon = when(option) {
        ExportOption.BACKUP -> FontIcon(MaterialDesign.MDI_FOLDER_MULTIPLE_OUTLINE)
        ExportOption.LISTEN -> FontIcon(MaterialDesign.MDI_PLAY)
        ExportOption.SOURCE_AUDIO -> FontIcon(Material.HEARING)
    }

    override fun updateItem(option: ExportOption?, empty: Boolean) {
        super.updateItem(option, empty)
        if (option == null || empty) {
            return
        }

        graphic = Button(messages[option.titleKey]).apply {
            addClass(
                "btn", "btn--tertiary", "btn--borderless", "export-menu__option-btn"
            )
            graphic = associatedIcon(option)
        }
    }
}

class DummyExportComboBoxButton : ListCell<String>() {
    override fun updateItem(item: String?, btl: Boolean) {
        super.updateItem(item, btl)
        if (item != null || !btl) {
            graphic = Button(item).apply {
                addClass(
                    "btn", "btn--tertiary", "btn--borderless", "dummy-export-menu__btn"
                )
                graphic = FontIcon(MaterialDesign.MDI_FILE_EXPORT)
            }
        }
    }
}