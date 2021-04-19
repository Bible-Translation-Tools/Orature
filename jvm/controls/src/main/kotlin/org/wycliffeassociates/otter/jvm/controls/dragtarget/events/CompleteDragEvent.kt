package org.wycliffeassociates.otter.jvm.controls.dragtarget.events

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class CompleteDragEvent(
    val take: Take,
    val onCancel: () -> Unit
) : Event(COMPLETE_DRAG) {
    companion object {
        val COMPLETE_DRAG: EventType<CompleteDragEvent> = EventType("COMPLETE_DRAG")
    }
}