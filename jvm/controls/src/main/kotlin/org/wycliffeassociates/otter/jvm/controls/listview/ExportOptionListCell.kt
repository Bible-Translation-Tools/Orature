package org.wycliffeassociates.otter.jvm.controls.listview

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import tornadofx.FX.Companion.messages
import tornadofx.addClass
import tornadofx.get
import tornadofx.toggleClass

class ExportOptionListCell : ListCell<ExportOption>() {

    private val node = Button().apply {
        addClass(
            "btn", "btn--tertiary", "btn--borderless", "export-menu__option-btn"
        )
        isMouseTransparent = true
    }

    private fun associatedIcon(option: ExportOption): FontIcon = when (option) {
        ExportOption.BACKUP -> FontIcon(MaterialDesign.MDI_FOLDER_MULTIPLE_OUTLINE)
        ExportOption.LISTEN -> FontIcon(MaterialDesign.MDI_PLAY)
        ExportOption.SOURCE_AUDIO -> FontIcon(Material.HEARING)
    }

    override fun updateItem(item: ExportOption?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            return
        }

        graphic = node.apply {
            text = messages[item.titleKey]
            graphic = associatedIcon(item)
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