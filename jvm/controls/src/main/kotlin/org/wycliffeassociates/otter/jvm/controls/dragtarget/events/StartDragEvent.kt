package org.wycliffeassociates.otter.jvm.controls.dragtarget.events

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import org.wycliffeassociates.otter.common.data.workbook.Take

class StartDragEvent(
    val mouseEvent: MouseEvent,
    val draggingNode: Node,
    val take: Take
) : Event(START_DRAG) {
    companion object {
        val START_DRAG: EventType<StartDragEvent> = EventType("START_DRAG")
    }
}
