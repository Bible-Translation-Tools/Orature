package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import tornadofx.FX

class LanguageTableRow : TableRow<Language>() {
    override fun updateItem(item: Language?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = false
        setOnMouseClicked {
            FX.eventbus.fire(LanguageSelectedEvent(item))
        }
    }
}