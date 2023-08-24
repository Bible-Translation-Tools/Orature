package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
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
                println("Width: ${new}px, Range to show: ${rangeToShow}")
                viewModel.getVerMarkersInRange(rangeToShow)
            }

            add(narrationWaveformLayer)

            add(VerseMarkersLayer().apply {
                isRecordingProperty.bind(viewModel.isRecordingProperty)
                markers.bind(viewModel.mockRecordedVerseMarkers) { it }
            })

//            scrollpane {
//                hbox {
//                    spacing = 10.0
//                    paddingHorizontal = 10.0
//
//                    bindChildren(viewModel.recordedVerses) { verse ->
//                        val index = viewModel.recordedVerses.indexOf(verse)
//                        val label = verse.label
//
//                        menubutton(label) {
//                            item("") {
//                                text = "Play"
//                                action {
//                                    fire(PlayVerseEvent(verse))
//                                }
//                                disableWhen {
//                                    viewModel.isRecordingProperty
//                                }
//                            }
//                            item("") {
//                                text = "Record Again"
//                                action {
//                                    fire(RecordAgainEvent(index))
//                                }
//                                disableWhen {
//                                    viewModel.isRecordingProperty
//                                }
//                            }
//                            item("") {
//                                text = "Open in..."
//                                action {
//                                    fire(OpenInAudioPluginEvent(index))
//                                }
//                                disableWhen {
//                                    viewModel.isRecordingProperty
//                                }
//                            }
//                        }
//                    }
//                }
//            }
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
    var totalRecordedFrames = SimpleIntegerProperty(0)
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
        val recordingStatus: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

        // TODO: Don't do this initialization here, or in this file. It is awkward.
        narrationViewModel.narrationIsInitialized.addListener {_, old, new ->
            if(new == true && isNarrationWaveformLayerInitialized.value == false) {

//                isRecordingProperty.addListener{_, old, new ->
//                    recordingStatus.onNext(new)
//                }
//
//                existingAndIncomingAudioRenderer = ExistingAndIncomingAudioRenderer(
//                    narrationViewModel.getExistingAudioFileReader(),
//                    narrationViewModel.getRecorderAudioStream(),
//                    recordingStatus,
//                    1920,
//                    10) // TODO: don't hardcode values!
//                waveform = Waveform(existingAndIncomingAudioRenderer!!)
                waveform = Waveform(narrationViewModel.existingAndIncomingAudioRenderer!!)

                volumeBar = VolumeBar(narrationViewModel.getRecorderAudioStream())
                waveform!!.isRecordingProperty.bind(isRecordingProperty)
                isNarrationWaveformLayerInitialized.set(true)
            }
        }

        narrationViewModel.recordStartProperty.addListener {_, old, new ->
            if(new == true && waveform?.renderer != null) {
                println("clearing render 2")
                waveform?.renderer?.clearData()
            }
        }

//        narrationViewModel.recordPauseProperty.addListener {_, old, new ->
//
//            println("=================================================pauseProperty: ${new}")
//            if(new == true) {
//                println("clearing render 1")
//                waveform?.renderer?.clearData()
//                println("filling with existing 2")
//                waveform?.renderer?.fillExistingAudioHolder()
//            }
//        }

    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }

    init {
//        narrationViewModel.isWritingToAudioFileProperty.addListener {_, old, new ->
//            if(old == true && new == false) {
//                println("ISWRITINGTOAUDIOFILEPROPERTY: ${new}==========================")
//                println("clearing render 1")
//                waveform?.renderer?.clearData()
//                println("filling with existing 2")
//                waveform?.renderer?.fillExistingAudioHolder()
//            }
//        }


//        narrationViewModel.recordedVerses.onChange {
//            println("detected changes in the recordedVerses list")
//            println("totalFrames: ${narrationViewModel.getExistingAudioFileReader().totalFrames}, seconds recorded: ${narrationViewModel.getExistingAudioFileReader().totalFrames / 44100}")
//        }
    }
}