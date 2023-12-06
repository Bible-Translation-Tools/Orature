package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableList
import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import tornadofx.*

class LanguageTableRow(
    private val unavailableLanguages: ObservableList<Language>
) : TableRow<Language>() {
    override fun updateItem(item: Language?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isDisable = item in unavailableLanguages
        isMouseTransparent = isDisable
        isFocusTraversable = !isDisable

        setOnMouseClicked {
            if (it.clickCount == 1) { // avoid double fire()
                FX.eventbus.fire(LanguageSelectedEvent(item))
            }
        }
    }
}