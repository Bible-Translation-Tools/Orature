/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.sun.javafx.util.Utils
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ScrollBar
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.jvm.controls.SCROLL_INCREMENT_UNIT
import org.wycliffeassociates.otter.jvm.controls.SCROLL_JUMP_UNIT
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkerControl
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.verse_markers_layer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.WaveformLayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.narration_waveform
import tornadofx.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class AudioWorkspaceView : View() {
    private val userIsDraggingProperty = SimpleBooleanProperty(false)

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

    val scrollBar = ScrollBar().apply {
        runLater {
            customizeScrollbarSkin()
        }

        disableWhen {
            viewModel.isRecordingProperty.or(viewModel.isPlayingProperty)
        }

        unitIncrement = SCROLL_INCREMENT_UNIT
        blockIncrementProperty().bind(maxProperty().doubleBinding {
            it?.let { maxValue ->
                maxValue.toDouble() / 10
            } ?: SCROLL_JUMP_UNIT
        })

        valueProperty().onChange { pos ->
            if (pos.toInt() != viewModel.audioPositionProperty.value) {
                viewModel.seekTo(pos.toInt())
            }
        }

        viewModel.audioPositionProperty.onChange { pos ->
            viewModel.scrollBarPositionProperty.set(pos.toDouble())
        }

        valueProperty().bindBidirectional(viewModel.scrollBarPositionProperty)
        maxProperty().bind(viewModel.totalAudioSizeProperty)
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
                verse_markers_layer {
                    verseMarkersControls.bind(markerNodes) { it }

                    var pos = 0

                    setOnDragStarted {
                        // caching current position on drag start
                        // to add delta to it later on drag continue
                        pos = viewModel.audioPositionProperty.value
                    }

                    setOnLayerScroll { delta ->
                        // Keep position inside audio bounds
                        val seekTo = Utils.clamp(0, pos + delta, viewModel.totalAudioSizeProperty.value)
                        viewModel.seekTo(seekTo)
                    }
                }
            }
            bottom = scrollBar
        }
    }


    override fun onDock() {
        super.onDock()
        viewModel.onDock()
        markerNodes.bind(viewModel.recordedVerses) { marker ->
            VerseMarkerControl().apply {
                visibleProperty().set(false)
                val markerLabel = when (marker) {
                    is ChapterMarker -> "c${marker.label}"
                    else -> marker.label
                }

                verseProperty.set(marker)
                verseIndexProperty.set(viewModel.recordedVerses.indexOf(marker))
                labelProperty.set(markerLabel)
                isRecordingProperty.bind(viewModel.isRecordingProperty)
                isRecordAgainPausedProperty.bind(viewModel.isRecordAgainPausedProperty)
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
    val isRecordAgainPausedProperty = SimpleBooleanProperty()
    val isPlayingProperty = SimpleBooleanProperty()
    var recordedVerses = observableListOf<AudioMarker>()

    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()

    val scrollBarPositionProperty = SimpleDoubleProperty()

    fun drawWaveform(context: GraphicsContext, canvas: Canvas, markerNodes: ObservableList<VerseMarkerControl>) {
        narrationViewModel.drawWaveform(context, canvas, markerNodes)
    }

    fun drawVolumeBar(context: GraphicsContext, canvas: Canvas) {
        narrationViewModel.drawVolumebar(context, canvas)
    }

    fun onDock() {
        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        isRecordAgainPausedProperty.bind(narrationViewModel.isRecordAgainPausedProperty)
        isPlayingProperty.bind(narrationViewModel.isPlayingProperty)
        totalAudioSizeProperty.bind(narrationViewModel.totalAudioSizeProperty)
        audioPositionProperty.bind(narrationViewModel.audioPositionProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }
    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }

    fun seekPercent(percent: Double) {
        narrationViewModel.seekPercent(percent)
    }

    fun seekTo(frame: Int) {
        narrationViewModel.seekTo(frame)
    }
}