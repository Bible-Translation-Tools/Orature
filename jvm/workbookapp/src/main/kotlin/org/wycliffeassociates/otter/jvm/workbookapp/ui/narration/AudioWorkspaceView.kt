package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.narration_waveform
import tornadofx.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AudioWorkspaceView : View() {
    private val viewModel: AudioWorkspaceViewModel by inject()

    private lateinit var narrationWaveformLayer: WaveformLayer

    val canvasInflatedProperty = SimpleBooleanProperty(false)


    private val drawables = mutableListOf<Drawable>()

    val executor = ThreadPoolExecutor(1, 1, 10000, TimeUnit.SECONDS, LinkedBlockingQueue())

    val runnable = Runnable {
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
    }

    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            executor.submit(runnable)
        }
    }

    override val root = stackpane {
        narration_waveform {
            narrationWaveformLayer = this

            canvasInflatedProperty.bind(
                widthProperty()
                    .greaterThan(0)
                    .and(heightProperty().greaterThan(0))
            )
        }
        scrollpane {
            vbox {
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
                hbox {
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
    var recordedVerses = observableListOf<VerseMarker>()

    fun drawWaveform(context: GraphicsContext, canvas: Canvas) {
        narrationViewModel.drawWaveform(context, canvas)
    }

    fun drawVolumeBar(context: GraphicsContext, canvas: Canvas) {
        narrationViewModel.drawVolumebar(context, canvas)
    }

    fun onDock() {
        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }

    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }
}