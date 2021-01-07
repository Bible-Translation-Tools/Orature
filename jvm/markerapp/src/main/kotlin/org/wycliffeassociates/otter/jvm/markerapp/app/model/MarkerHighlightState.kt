package org.wycliffeassociates.otter.jvm.markerapp.app.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty

class MarkerHighlightState {
    val visibility = SimpleBooleanProperty(false)
    val color = SimpleStringProperty("#00FF0020")
    val translate = SimpleDoubleProperty(0.0)
    val width = SimpleDoubleProperty(0.0)
}
