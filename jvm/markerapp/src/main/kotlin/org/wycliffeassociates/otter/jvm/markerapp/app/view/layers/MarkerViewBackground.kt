package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*

class MarkerViewBackground : BorderPane() {

    init {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            top {
                region {
                    styleClass.add("vm-waveform-frame__top-track")
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
                }
            }
        }
    }
}
