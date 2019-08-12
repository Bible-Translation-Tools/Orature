package org.wycliffeassociates.otter.jvm.app.widgets.takecard.events

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.input.MouseEvent
import org.wycliffeassociates.otter.common.data.workbook.Take

class CompleteDragEvent(
    val mouseEvent: MouseEvent,
    val take: Take,
    val onCancel: () -> Unit
): Event(COMPLETE_DRAG) {
    companion object {
        val COMPLETE_DRAG: EventType<CompleteDragEvent> = EventType("COMPLETE_DRAG")
    }
}