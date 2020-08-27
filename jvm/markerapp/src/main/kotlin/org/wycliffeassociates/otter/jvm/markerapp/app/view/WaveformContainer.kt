package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import tornadofx.*

class WaveformContainer : Fragment() {
    override val root = stackpane {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        add(PlaceMarkerLayer())
    }
}
