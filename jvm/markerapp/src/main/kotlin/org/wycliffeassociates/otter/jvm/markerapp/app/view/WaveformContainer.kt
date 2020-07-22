package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.scene.Parent
import javafx.scene.layout.Priority
import tornadofx.*

class WaveformContainer: Fragment() {
    override val root = stackpane {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
    }
}