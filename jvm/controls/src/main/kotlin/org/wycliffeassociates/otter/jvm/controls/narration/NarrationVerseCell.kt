package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.Verse
import tornadofx.addClass

class NarrationVerseCell : ListCell<Verse>() {
    private val view = NarrationVerseItem()

    private val onRecordActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: Verse?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        view.isActiveProperty.set(isSelected)
        view.isLastVerseProperty.set(index == listView.items.lastIndex)

        graphic = view.apply {
            verseLabelProperty.set(item.label)
            verseTextProperty.set(item.text)

            onRecordActionProperty.set(onRecordActionCellProperty.value)

            setOnNextVerse {
                listView.selectionModel.selectNext()
                listView.scrollTo(item)
            }
        }
    }

    fun setOnRecord(op: () -> Unit) {
        onRecordActionCellProperty.set(EventHandler {
            op.invoke()
        })
    }
}