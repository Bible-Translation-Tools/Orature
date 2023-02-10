package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.Verse
import tornadofx.addClass

class NarrationRecordCell : ListCell<Verse>() {
    private val view = NarrationRecordItem()

    private val onPlayActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onOpenAppActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onRecordAgainActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("narration-record__verse-cell")
    }

    override fun updateItem(item: Verse?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            verseLabelProperty.set(item.label)

            onPlayActionProperty.set(onPlayActionCellProperty.value)
            onOpenAppActionProperty.set(onOpenAppActionCellProperty.value)
            onRecordAgainActionProperty.set(onRecordAgainActionCellProperty.value)
        }
    }

    fun setOnPlay(op: () -> Unit) {
        onPlayActionCellProperty.set(EventHandler {
            op.invoke()
        })
    }

    fun setOnOpenApp(op: () -> Unit) {
        onOpenAppActionCellProperty.set(EventHandler {
            op.invoke()
        })
    }

    fun setOnRecordAgain(op: () -> Unit) {
        onRecordAgainActionCellProperty.set(EventHandler {
            op.invoke()
        })
    }
}