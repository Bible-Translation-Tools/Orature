package org.wycliffeassociates.otter.jvm.markerapp.app.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty

class MarkerHighlightState {
    val visibility = SimpleBooleanProperty(false)
    val styleClass = SimpleStringProperty("vm-highlight-primary")
    val primaryStyleClass = SimpleStringProperty("vm-highlight-primary")
    val secondaryStyleClass = SimpleStringProperty("vm-highlight-secondary")
    val translate = SimpleDoubleProperty(0.0)
    val width = SimpleDoubleProperty(0.0)
}
