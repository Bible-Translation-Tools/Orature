package org.wycliffeassociates.otter.jvm.controls.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty

class MarkerHighlightState {
    val visibility = SimpleBooleanProperty(false)
    val styleClass = SimpleStringProperty("scrolling-waveform__highlight--primary")
    val primaryStyleClass = SimpleStringProperty("scrolling-waveform__highlight--primary")
    val secondaryStyleClass = SimpleStringProperty("scrolling-waveform__highlight--secondary")
    val translate = SimpleDoubleProperty(0.0)
    val width = SimpleDoubleProperty(0.0)
}
