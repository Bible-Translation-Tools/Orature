package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.event.Event
import javafx.event.EventType

class TakeEvent(type: EventType<TakeEvent>) : Event(type) {

    companion object {
        val PLAY: EventType<TakeEvent> = EventType("PLAY")
        val PAUSE: EventType<TakeEvent> = EventType("PAUSE")
    }
}
