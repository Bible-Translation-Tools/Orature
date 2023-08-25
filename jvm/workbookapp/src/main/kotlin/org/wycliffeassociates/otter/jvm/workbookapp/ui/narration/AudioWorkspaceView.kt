package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkersLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.ExistingAndIncomingAudioRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.Waveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import tornadofx.*

class AudioWorkspaceView : View() {
    private val viewModel: AudioWorkspaceViewModel by inject()
    var narrationWaveformLayer = WaveformLayer()

    override val root =
        stackpane {

            add(narrationWaveformLayer.apply {
                // Initializes the waveform and volumeBar used in the waveform layer
                // TODO: Don't do this initialization here, or in this file. It is awkward.
                viewModel.isWaveformRendererInitialized.addListener {_, old, new ->
                    if(new == true) {

                        waveform = Waveform(viewModel.waveformRenderer!!)
                        waveform!!.isRecordingProperty.bind(viewModel.isRecordingProperty)
                        volumeBar = VolumeBar(viewModel.waveformRenderer!!.incomingAudioStream)
                        audioFilePositionProperty.bind(viewModel.audioFilePositionProperty)

                        waveform?.heightProperty?.bind(this.heightProperty())
                        waveform?.widthProperty?.bind(this.widthProperty())
                        canvasFragment.drawableProperty.set(waveform)

                        volumeBarCanavsFragment.drawableProperty.set(volumeBar)
                        volumeBarCanavsFragment.isDrawingProperty.set(true)
                        isNarrationWaveformLayerInitialized.set(true) // should probably be a binding
                    }
                }
            })

            add(VerseMarkersLayer().apply {
                isRecordingProperty.bind(viewModel.isRecordingProperty)
                markers.bind(viewModel.mockRecordedVerseMarkers) { it }
                rightOffset.bind(viewModel.pxFromIncomingAudio)

                // Updates the audioFilePosition property when the scrollbar position changes
                scrollBarPositionProperty.addListener { _, old, new ->
                    viewModel.audioFilePositionProperty.set(pixelsToFrames(scrollBarPositionProperty.value))
                }

            })

        }


    override fun onDock() {
        super.onDock()
        viewModel.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onUndock()
    }
}


class AudioWorkspaceViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(AudioWorkspaceViewModel::class.java)

    private val narrationViewModel: NarrationViewModel by inject()

    val isRecordingProperty = SimpleBooleanProperty()
    var recordedVerses = observableListOf<VerseMarker>()

    var mockRecordedVerseMarkers = observableListOf<VerseMarker>()
    var pxFromIncomingAudio = SimpleIntegerProperty(0)
    var audioFilePositionProperty = SimpleIntegerProperty(0)
    var waveformRenderer : ExistingAndIncomingAudioRenderer? = null
    var isWaveformRendererInitialized = SimpleBooleanProperty(false)

    fun onDock() {


        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }

        mockRecordedVerseMarkers.bind(narrationViewModel.mockRecordedVerseMarkers) { it }

        narrationViewModel.existingAndIncomingAudioRendererIsInitialized.addListener {_, old, new ->
            if(new == true && isWaveformRendererInitialized.value == false) {
                waveformRenderer = narrationViewModel.existingAndIncomingAudioRenderer
                pxFromIncomingAudio.bind(waveformRenderer!!.bytesFromIncoming.div(2).div(229))
                isWaveformRendererInitialized.set(true)
            }

        }

        // clears waveform on chapter reset
        narrationViewModel.recordStartProperty.addListener {_, old, new ->
            if(new == true && waveformRenderer != null) {
                waveformRenderer?.clearData()
            }
        }

    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }

}