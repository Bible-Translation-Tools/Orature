package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import javafx.beans.property.SimpleBooleanProperty
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

    init {

        // TODO: Create renderer
        // TODO: Create Waveform

        this.maxWidth = 1895.0 // TODO: stop hardcoding
        canvasFragment.prefWidthProperty().bind(this.widthProperty().minus(25)) // TODO: stop hardcoding
        canvasFragment.maxWidth(1920.0) // TODO: stop hardcoding

        canvasFragment.let {
            style {
                backgroundColor += c("#E5E8EB")
            }
        }

        isNarrationWaveformLayerInitialized.addListener {_, old, new ->
            if(new == true) {
                waveform?.heightProperty?.bind(this.heightProperty())
                waveform?.widthProperty?.bind(this.widthProperty())
                canvasFragment.drawableProperty.set(waveform)
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
            isNarrationWaveformLayerInitialized.addListener {_, old, new ->
                if(new == true) {
                    volumeBarCanavsFragment.drawableProperty.set(volumeBar)
                    volumeBarCanavsFragment.isDrawingProperty.set(true)
                }
            }
            add(volumeBarCanavsFragment)
        }
    }
}