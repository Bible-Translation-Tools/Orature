package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.sun.glass.ui.Screen
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ScrollBar
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.narration_waveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.pixelsToFrames
import tornadofx.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class AudioWorkspaceView : View() {
    private val logger = LoggerFactory.getLogger(AudioWorkspaceView::class.java)

    private val viewModel: AudioWorkspaceViewModel by inject()

    private lateinit var narrationWaveformLayer: WaveformLayer

    val canvasInflatedProperty = SimpleBooleanProperty(false)

    private val drawables = mutableListOf<Drawable>()

    val executor = ThreadPoolExecutor(1, 1, 10000, TimeUnit.SECONDS, LinkedBlockingQueue())

    val finishedFrame = AtomicBoolean(true)

    val runnable = Runnable {
        if (finishedFrame.compareAndExchange(true, false)) {
            try {
                if (canvasInflatedProperty.value) {
                    viewModel.drawWaveform(
                        narrationWaveformLayer.getWaveformContext(),
                        narrationWaveformLayer.getWaveformCanvas()
                    )
                    viewModel.drawVolumeBar(
                        narrationWaveformLayer.getVolumeBarContext(),
                        narrationWaveformLayer.getVolumeCanvas()
                    )
                }
            } catch (e: Exception) {
                logger.error("Exception in render loop", e)
            }
            finally {
                finishedFrame.set(true)
            }
        }
    }

    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            executor.submit(runnable)
        }
    }

    override val root = stackpane {
        subscribe<AppCloseRequestEvent> {
            at.stop()
        }

        borderpane {
            center = narration_waveform {
                narrationWaveformLayer = this

                canvasInflatedProperty.bind(
                    widthProperty()
                        .greaterThan(0)
                        .and(heightProperty().greaterThan(0))
                )
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
        narrationViewModel.scrollAudio(delta)
    }

    fun drawWaveform(context: GraphicsContext, canvas: Canvas) {
        narrationViewModel.drawWaveform(context, canvas)
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