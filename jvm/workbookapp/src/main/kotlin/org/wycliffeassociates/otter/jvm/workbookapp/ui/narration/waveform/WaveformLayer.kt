package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.narration.CanvasFragment
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import tornadofx.*


// Thing that is the container for the Waveform and VolumeBar
class WaveformLayer : HBox() {

    var canvasFragment = CanvasFragment()
    var volumeBarCanavsFragment = CanvasFragment()
    var isNarrationWaveformLayerInitialized = SimpleBooleanProperty(false)
    var waveform : Waveform? = null
    var volumeBar : VolumeBar? = null
    var audioFilePositionProperty = SimpleIntegerProperty(0)
    val volumeBarWidth = 25
    val maxScreenWidth = 1920.0

    init {
        audioFilePositionProperty.addListener {_, old, new ->
            waveform?.renderer?.existingAudioReader?.seek(maxOf(audioFilePositionProperty.value, 0))
            println("seeking to ${audioFilePositionProperty.value}")
            waveform?.renderer?.fillExistingAudioHolder()
        }

        this.maxWidth = maxScreenWidth - volumeBarWidth
        canvasFragment.prefWidthProperty().bind(this.widthProperty().minus(volumeBarWidth))
        canvasFragment.maxWidth(maxScreenWidth)

        canvasFragment.let {
            style {
                backgroundColor += c("#E5E8EB")
            }
        }

        canvasFragment.isDrawingProperty.set(true)
        add(canvasFragment)

        hbox {
            prefWidth = 25.0
            volumeBarCanavsFragment.let {
                style {
                    backgroundColor += c("#001533")
                }
            }
            volumeBarCanavsFragment.prefWidthProperty().bind(this.widthProperty())
            add(volumeBarCanavsFragment)
        }
    }
}