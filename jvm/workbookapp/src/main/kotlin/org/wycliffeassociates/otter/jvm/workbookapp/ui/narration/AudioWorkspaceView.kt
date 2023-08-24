package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkersLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.Waveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import tornadofx.*

class AudioWorkspaceView : View() {
    private val viewModel: AudioWorkspaceViewModel by inject()
    var narrationWaveformLayer = WaveformLayer()

    override val root =
        stackpane {
            // Initializes the waveform and volumeBar used in the waveform layer
            // TODO: Don't do this initialization here, or in this file. It is awkward.
            viewModel.isNarrationWaveformLayerInitialized.addListener {_, old, new ->
                if(new == true) {
                    narrationWaveformLayer.waveform = viewModel.waveform
                    narrationWaveformLayer.volumeBar = viewModel.volumeBar
                    narrationWaveformLayer.isNarrationWaveformLayerInitialized.set(true)
                }
            }

            narrationWaveformLayer.widthProperty().addListener { _, old, new ->
                var rangeToShow = viewModel.getCurrentFrameRangeShown(0, new.toInt())
                viewModel.getVerMarkersInRange(rangeToShow)
            }

            add(narrationWaveformLayer)

            add(VerseMarkersLayer().apply {
                isRecordingProperty.bind(viewModel.isRecordingProperty)
                markers.bind(viewModel.mockRecordedVerseMarkers) { it }
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

    var isNarrationWaveformLayerInitialized = SimpleBooleanProperty(false)
    var waveform : Waveform? = null
    var volumeBar : VolumeBar? = null
    var mockRecordedVerseMarkers = observableListOf<VerseMarker>()

    fun getCurrentFrameRangeShown(relativePosition: Int, screenWidth: Int) : IntRange {
        var framesPerPixel = 229 // TODO: don't use constants here
        return relativePosition .. (relativePosition + (screenWidth*framesPerPixel))
    }

    fun getVerMarkersInRange(rangeToShow: IntRange): List<VerseMarker> {
        val verseMarkersToShow = mutableListOf<VerseMarker>()
        for (i in 0 until mockRecordedVerseMarkers.size) {
            val currentVerse = mockRecordedVerseMarkers[i]
            if (currentVerse.location in rangeToShow) {
                println("verseMarker: ${currentVerse.label}, marker.end: ${currentVerse.end}")
                verseMarkersToShow.add(currentVerse)
            }
        }
        return verseMarkersToShow
    }

    fun onDock() {


        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }
        mockRecordedVerseMarkers.bind(narrationViewModel.mockRecordedVerseMarkers) { it }

        // TODO: Don't do this initialization here, or in this file. It is awkward.
        narrationViewModel.narrationIsInitialized.addListener {_, old, new ->
            if(new == true && isNarrationWaveformLayerInitialized.value == false) {
                waveform = Waveform(narrationViewModel.existingAndIncomingAudioRenderer!!)

                volumeBar = VolumeBar(narrationViewModel.getRecorderAudioStream())
                waveform!!.isRecordingProperty.bind(isRecordingProperty)
                isNarrationWaveformLayerInitialized.set(true)
            }
        }

        // clears waveform on chapter reset
        narrationViewModel.recordStartProperty.addListener {_, old, new ->
            if(new == true && waveform?.renderer != null) {
                println("clearing render 2")
                waveform?.renderer?.clearData()
            }
        }


    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }

}