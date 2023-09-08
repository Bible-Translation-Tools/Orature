package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ScrollBar
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkerControl
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkersLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.narration_waveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.pixelsToFrames
import tornadofx.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AudioWorkspaceView : View() {
    private val logger = LoggerFactory.getLogger(AudioWorkspaceView::class.java)

    private val viewModel: AudioWorkspaceViewModel by inject()

    private lateinit var narrationWaveformLayer: WaveformLayer

    val canvasInflatedProperty = SimpleBooleanProperty(false)

    private val drawables = mutableListOf<Drawable>()

    val jobQueue = LinkedBlockingQueue<Runnable>()
    val executor = ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, jobQueue)

    val finishedFrame = AtomicBoolean(true)

    val markerNodes = observableListOf<VerseMarkerControl>()

    val runnable = Runnable {
        if (finishedFrame.compareAndExchange(true, false)) {
            try {
                if (canvasInflatedProperty.value) {
                    viewModel.drawWaveform(
                        narrationWaveformLayer.getWaveformContext(),
                        narrationWaveformLayer.getWaveformCanvas(),
                        markerNodes
                    )
                    viewModel.drawVolumeBar(
                        narrationWaveformLayer.getVolumeBarContext(),
                        narrationWaveformLayer.getVolumeCanvas()
                    )
                }
            } catch (e: Exception) {
                logger.error("Exception in render loop", e)
            } finally {
                finishedFrame.set(true)
            }
        }
    }

    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            executor.submit(runnable)
        }
    }

    init {
        tryImportStylesheet("/css/verse-markers-layer.css")
    }

    override val root = stackpane {
        subscribe<AppCloseRequestEvent> {
            at.stop()
            jobQueue.clear()
            executor.shutdownNow()
        }


        borderpane {
            center = stackpane {
                narration_waveform {
                    narrationWaveformLayer = this

                    canvasInflatedProperty.bind(
                        widthProperty()
                            .greaterThan(0)
                            .and(heightProperty().greaterThan(0))
                    )
                }
                borderpane {
                    bindChildren(markerNodes) {
                        it.apply {
                            minHeightProperty().bind(narrationWaveformLayer.heightProperty())
                            prefHeightProperty().bind(narrationWaveformLayer.heightProperty())
                        }
                    }
                }
            }
            bottom = ScrollBar().apply {
                viewModel.audioPositionProperty.onChange {
                    value = framesToPixels(it).toDouble()
                }

                maxProperty().bind(viewModel.totalAudioSizeProperty)

                var lastModified = 0L

                valueProperty().onChange {
                    if (viewModel.isPlayingProperty.value == false && viewModel.isRecordingProperty.value == false) {
                        if (System.currentTimeMillis() - lastModified > 75L) {
                            lastModified = System.currentTimeMillis()
                            viewModel.scrollAudio(pixelsToFrames(it))
                        }
                    }
                }
            }
        }

//            borderpane {
//                bindChildren(markerNodes) {
//                    it.apply {
//                        minHeightProperty().bind(narrationWaveformLayer.heightProperty())
//                        prefHeightProperty().bind(narrationWaveformLayer.heightProperty())
//                    }
//                }
//            }

//        add(VerseMarkersLayer().apply {
//            verseMarkersControls.bind(markerNodes) { it }
//        })

        hbox {
            maxHeight = 50.0
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
                        }
                        item("") {
                            text = "Record Again"
                            action {
                                fire(RecordAgainEvent(index))
                            }
                        }
                        item("") {
                            text = "Open in..."
                            action {
                                fire(OpenInAudioPluginEvent(index))
                            }
                        }
                    }
                }
            }

            button("Play All") {
                action {
                    fire(PlayChapterEvent())
                }
            }
            button("Pause") {
                action {
                    fire(PauseEvent())
                }
            }
        }
    }


    override fun onDock() {
        super.onDock()
        viewModel.onDock()
//        viewModel.recordedVerses.onChangeAndDoNowWithDisposer { markers ->
//            markerNodes.clear()
//            markers.mapIndexed { index, marker ->
//
//            }.let {
//                markerNodes.addAll(it)
//            }
//        }
        markerNodes.bind(viewModel.recordedVerses) { marker ->
            VerseMarkerControl().apply {
                verseProperty.set(marker)
                // verseIndexProperty.set(index)
                labelProperty.set(marker.label)
                isRecordingProperty.bind(viewModel.isRecordingProperty)
            }
        }

        at.start()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onUndock()
        at.stop()
    }
}

class AudioWorkspaceViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(AudioWorkspaceViewModel::class.java)

    private val narrationViewModel: NarrationViewModel by inject()

    val isRecordingProperty = SimpleBooleanProperty()
    val isPlayingProperty = SimpleBooleanProperty()
    var recordedVerses = observableListOf<VerseMarker>()

    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()

    fun scrollAudio(delta: Int) {
        narrationViewModel.seekAudio(delta)
    }

    fun drawWaveform(context: GraphicsContext, canvas: Canvas, markerNodes: ObservableList<VerseMarkerControl>) {
        narrationViewModel.drawWaveform(context, canvas, markerNodes)
    }

    fun drawVolumeBar(context: GraphicsContext, canvas: Canvas) {
        narrationViewModel.drawVolumebar(context, canvas)
    }

    fun onDock() {
        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        isPlayingProperty.bind(narrationViewModel.isPlayingProperty)
        totalAudioSizeProperty.bind(
            narrationViewModel.totalAudioSizeProperty.integerBinding {
                it?.let {
                    framesToPixels(it.toInt())
                } ?: 0
            }
        )
        audioPositionProperty.bind(narrationViewModel.audioPositionProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }

    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }
}