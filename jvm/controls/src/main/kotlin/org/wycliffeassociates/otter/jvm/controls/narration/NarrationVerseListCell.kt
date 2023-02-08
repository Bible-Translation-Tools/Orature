package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.geometry.Pos
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.Verse
import tornadofx.addClass

class NarrationVerseListCell : ListCell<Verse>() {
    private val view = NarrationVerseItem()

    init {
        addClass("narration-list__verse-cell")
        alignment = Pos.TOP_CENTER
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

            setOnRecord {
                println("Recording...")
            }

            setOnNextVerse {
                listView.selectionModel.selectNext()
                listView.scrollTo(item)
            }
        }
    }
}