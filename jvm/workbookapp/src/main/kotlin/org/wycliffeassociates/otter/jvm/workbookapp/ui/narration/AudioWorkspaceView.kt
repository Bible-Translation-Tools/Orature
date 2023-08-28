package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkerControl
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkersLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.ExistingAndIncomingAudioRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.Waveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import tornadofx.*

class AudioWorkspaceView : View(), Drawable {
    private val viewModel: AudioWorkspaceViewModel by inject()
    private var narrationWaveformLayer = WaveformLayer()
    val layerWidthProperty = SimpleDoubleProperty(0.0)

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

                        volumeBarCanavsFragment.drawableProperty.set(volumeBar)
                        volumeBarCanavsFragment.isDrawingProperty.set(true)
                        isNarrationWaveformLayerInitialized.set(true) // should probably be a binding
                    }
                }
            })

            add(VerseMarkersLayer().apply {
                isRecordingProperty.bind(viewModel.isRecordingProperty)
                markers.bind(viewModel.recordedVerses) { it }
                rightOffset.bind(viewModel.pxFromIncomingAudio)
                verseMarkersControls.bind(viewModel.verseMarkersControls) { it }

                layerWidthProperty.bind(this.widthProperty())

                // Updates the VerseMarkerControl list when the mockRecorderverseMarkers changes
                viewModel.recordedVerses.onChange {
                    viewModel.verseMarkersControls.clear()

                    for(i in 0 until viewModel.recordedVerses.size) {
                        val newVerseMarkerControl = VerseMarkerControl()
                        newVerseMarkerControl.verseProperty.set(viewModel.recordedVerses[i])
                        newVerseMarkerControl.verseIndexProperty.set(i)
                        newVerseMarkerControl.labelProperty.set((i+1).toString())

                        newVerseMarkerControl.isRecordingProperty.bind(isRecordingProperty)

                        viewModel.verseMarkersControls.add(newVerseMarkerControl)
                    }
                }


                // Updates the audioFilePosition property when the scrollbar position changes
                scrollBarPositionProperty.addListener { _, old, new ->
                    viewModel.audioFilePositionProperty.set(pixelsToFrames(scrollBarPositionProperty.value))
                }

            })

        }

    val rangeOfAudioToShowStart = Bindings.createIntegerBinding(
        {
            return@createIntegerBinding viewModel.audioFilePositionProperty.value + pixelsToFrames((viewModel.pxFromIncomingAudio.value.toDouble()))
        }, // TODO: finish
        viewModel.audioFilePositionProperty, viewModel.pxFromIncomingAudio
    )
    val rangeOfAudioToShowEnd = Bindings.createIntegerBinding(
        {
            (rangeOfAudioToShowStart.value + pixelsToFrames(layerWidthProperty.value))
        },
        layerWidthProperty, rangeOfAudioToShowStart
    )

    fun updateVerseMarkersInFrameRange(start: Int, end: Int) {
        Platform.runLater {
//            println("===== start frame: ${start}, end frame: ${end} =====")
            for (i in 0 until viewModel.verseMarkersControls.size) {
                val currentVerseMarkerControl = viewModel.verseMarkersControls[i]
                if (currentVerseMarkerControl.verseProperty.value.location in start..end) {

//                    println("showing ${currentVerseMarkerControl.verseProperty.value.label} at frame position: ${currentVerseMarkerControl.verseProperty.value.location }")

                    // TODO: update all markers position on the

                    // TODO: update marker's visibility
                    //currentVerseMarkerControl.visibleProperty().set(true)

                } else {
//                    currentVerseMarkerControl.visibleProperty().set(false)
                }
            }
        }
    }

    override fun draw(context: GraphicsContext, canvas: Canvas){
//        println("drawing waveform")
        narrationWaveformLayer.waveform?.draw(context, canvas)

        // Updating verse markers X position here
//        println("Updating verse markers positions")
        updateVerseMarkersInFrameRange(rangeOfAudioToShowStart.value, rangeOfAudioToShowEnd.value)

    }

    override fun onDock() {
        super.onDock()
        viewModel.onDock()
        narrationWaveformLayer.canvasFragment.drawableProperty.set(this)
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

//    var mockRecordedVerseMarkers = observableListOf<VerseMarker>()
    var pxFromIncomingAudio = SimpleIntegerProperty(0)
    var audioFilePositionProperty = SimpleIntegerProperty(0)
    var waveformRenderer : ExistingAndIncomingAudioRenderer? = null
    var isWaveformRendererInitialized = SimpleBooleanProperty(false)
    val verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()

    fun onDock() {


        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.mockRecordedVerseMarkers) { it }

//        mockRecordedVerseMarkers.bind(narrationViewModel.mockRecordedVerseMarkers) { it }

        narrationViewModel.existingAndIncomingAudioRendererIsInitialized.addListener {_, old, new ->
            if(new == true && isWaveformRendererInitialized.value == false) {
                waveformRenderer = narrationViewModel.existingAndIncomingAudioRenderer
                pxFromIncomingAudio.bind(waveformRenderer!!.bytesFromIncomingProperty.div(2).div(229))
                waveformRenderer?.fillExistingAudioHolder()
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