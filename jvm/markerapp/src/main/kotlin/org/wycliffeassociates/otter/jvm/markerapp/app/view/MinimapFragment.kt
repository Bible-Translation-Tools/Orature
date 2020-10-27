package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class MinimapFragment : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()

    val slider = AudioSlider().apply {
        player.set(viewModel.audioPlayer)
        secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
    }

    override val root = hbox {
        alignment = Pos.CENTER_LEFT
        styleClass.add("vm-minimap-container")
        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            button {
                graphic = FontIcon("gmi-bookmark")
                styleClass.add("vm-marker-count__icon")
            }
            add(
                label().apply {
                    textProperty().bind(viewModel.markerRatioProperty)
                }
            )
        }
        add(
            slider.apply {
                hgrow = Priority.ALWAYS
            }
        )
    }
}
