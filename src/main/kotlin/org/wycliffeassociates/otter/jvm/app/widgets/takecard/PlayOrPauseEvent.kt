package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.event.Event
import javafx.event.EventType

class PlayOrPauseEvent(type: EventType<PlayOrPauseEvent>) : Event(type) {

    companion object {
        val PLAY: EventType<PlayOrPauseEvent> = EventType("PLAY")
        val PAUSE: EventType<PlayOrPauseEvent> = EventType("PAUSE")
    }
}
