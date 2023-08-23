package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.ExistingAndIncomingAudioRenderer
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
                println("Width: ${new}px, Range to show: ${viewModel.getCurrentFrameRangeShown(0, new.toInt())}")
            }

            add(narrationWaveformLayer)

            scrollpane {
                hbox {
                    spacing = 10.0
                    paddingHorizontal = 10.0

                    bindChildren(viewModel.recordedVerses) { verse ->
                        val index = viewModel.recordedVerses.indexOf(verse)
                        val label = verse.label

                        menubutton(label) {
                            item("") {
                                text = "Play"
                                action {
                                    fire(PlayVerseEvent(verse))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Record Again"
                                action {
                                    fire(RecordAgainEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Open in..."
                                action {
                                    fire(OpenInAudioPluginEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                        }
                    }
                }
            }
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
    var existingAndIncomingAudioRenderer : ExistingAndIncomingAudioRenderer? = null

    fun getCurrentFrameRangeShown(relativePosition: Int, screenWidth: Int) : IntRange {
        var framesPerPixel = 229 // TODO: don't use constants here
        return relativePosition .. (relativePosition + (screenWidth*framesPerPixel))
    }

    // TODO: call this on widthProperty change
    fun getVerMarkersInRange(verseMarker: VerseMarker, rangeToShow: IntRange): List<VerseMarker> {
        val verseMarkersToShow = mutableListOf<VerseMarker>()
        for (i in 0 until narrationViewModel.recordedVerses.size) {
            val currentVerse = narrationViewModel.recordedVerses[i]
            if (currentVerse.end in rangeToShow) {
                println("verseMarker: ${currentVerse.label}, marker.end: ${currentVerse.end}")
                verseMarkersToShow.add(currentVerse)
            }
        }
        return verseMarkersToShow
    }

    fun onDock() {


        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }


        // TODO: Don't do this initialization here, or in this file. It is awkward.
        narrationViewModel.narrationIsInitialized.addListener {_, old, new ->
            if(new == true && isNarrationWaveformLayerInitialized.value == false) {
                val alwaysRecordingStatus: Observable<Boolean> = Observable.just(true)

                existingAndIncomingAudioRenderer = ExistingAndIncomingAudioRenderer(
                    narrationViewModel.getExistingAudioFileReader(),
                    narrationViewModel.getRecorderAudioStream(),
                    alwaysRecordingStatus,
                    1920,
                    10) // TODO: don't hardcode values!
                waveform = Waveform(existingAndIncomingAudioRenderer!!)

                volumeBar = VolumeBar(narrationViewModel.getRecorderAudioStream())
                waveform!!.isRecordingProperty.bind(isRecordingProperty)
                isNarrationWaveformLayerInitialized.set(true)
            }
        }

        narrationViewModel.recordStartProperty.addListener {_, old, new ->
            if(new == true && existingAndIncomingAudioRenderer != null) {
                waveform?.renderer?.clearData()
            }
        }

    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }

    init {
        narrationViewModel.recordedVerses.onChange {
            println("detected changes in the recordedVerses list")
            println("totalFrames: ${narrationViewModel.getExistingAudioFileReader().totalFrames}, seconds recorded: ${narrationViewModel.getExistingAudioFileReader().totalFrames / 44100}")
        }
    }
}