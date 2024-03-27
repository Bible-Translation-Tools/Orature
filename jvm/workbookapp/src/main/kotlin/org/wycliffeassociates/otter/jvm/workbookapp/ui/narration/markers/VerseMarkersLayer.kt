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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.event.EventTarget
import javafx.scene.layout.BorderPane
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType

class VerseMarkersLayer : BorderPane() {
    private val logger = LoggerFactory.getLogger(VerseMarkersLayer::class.java)
    val markers = observableListOf<VerseMarker>()

    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()

    val verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()
    private val onScrollProperty = SimpleObjectProperty<(Int) -> Unit>()
    private val onDragStartedProperty = SimpleObjectProperty<() -> Unit>()

    init {
        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        var scrollDelta = 0.0
        var scrollOldPos = 0.0

        setOnMousePressed { event ->
            val point = localToParent(event.x, event.y)
            scrollOldPos = point.x
            scrollDelta = 0.0
            onDragStartedProperty.value?.invoke()
            event.consume()
        }

        setOnMouseDragged { event ->
            val point = localToParent(event.x, event.y)
            scrollDelta = scrollOldPos - point.x

            val frameDelta = pixelsToFrames(scrollDelta, width = this@VerseMarkersLayer.width.toInt())
            onScrollProperty.value?.invoke(frameDelta)
            event.consume()
        }

        setOnMouseReleased { event ->
            scrollDelta = 0.0
            scrollOldPos = 0.0
            event.consume()
        }

        bindChildren(verseMarkersControls) { verseMarkerControl ->

            verseMarkerControl.apply {
                val dragTarget = dragAreaProperty.value

                dragTarget.mouseTransparentProperty().bind(
                    Bindings.createBooleanBinding(
                        {
                            narrationStateProperty.value?.let {
                                it == NarrationStateType.RECORDING_PAUSED
                            } ?: false
                        },
                        narrationStateProperty
                    )
                )


                var delta = 0.0
                var oldPos = 0.0

                minHeightProperty().bind(this@VerseMarkersLayer.heightProperty())
                prefHeightProperty().bind(this@VerseMarkersLayer.heightProperty())

                dragTarget.setOnMousePressed { event ->

                    event.consume()
                    userIsDraggingProperty.set(true)
                    if (!canBeMovedProperty.value) return@setOnMousePressed
                    delta = 0.0
                    oldPos = layoutX

                    FX.eventbus.fire(
                        NarrationMovingMarkerEvent(verseMarkerControl.verseIndexProperty.value)
                    )
                }

                dragTarget.setOnMouseDragged { event ->
                    event.consume()
                    userIsDraggingProperty.set(true)
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    try {
                        val point = localToParent(event.x, event.y)
                        val (start, end) = verseBoundaries(verseIndexProperty.value)
                        val currentPos = point.x.coerceIn(start..end)

                        delta = currentPos - oldPos

                        layoutX = currentPos
                    } catch (e: Exception) {
                        // This can prevent attempting to move a marker that was originally created too close together
                        // if the user spammed next verse while recording. Moving surrounding markers around will allow
                        // for this marker to get enough space to move around.
                        logger.error("Tried to move a marker, but aborted", e)
                    }
                }

                dragTarget.setOnMouseReleased { event ->
                    if (delta != 0.0) {
                        val frameDelta = pixelsToFrames(delta, width = this@VerseMarkersLayer.width.toInt())
                        FX.eventbus.fire(
                            NarrationMarkerChangedEvent(
                                verseMarkerControl.verseIndexProperty.value,
                                frameDelta
                            )
                        )
                    }
                    userIsDraggingProperty.set(false)
                    event.consume()
                }
            }
        }
    }

    private fun verseBoundaries(verseIndex: Int): Pair<Double, Double> {
        val previousVerse = verseMarkersControls.getOrNull(verseIndex - 1)
        val startBounds = if (previousVerse != null && previousVerse.visibleProperty().value) {
            previousVerse.layoutX + (MARKER_AREA_WIDTH * 4)
        } else 0.0
        val nextVerse = verseMarkersControls.getOrNull(verseIndex + 1)
        val endBounds = if (nextVerse != null && nextVerse.visibleProperty().value) {
            nextVerse.layoutX - (MARKER_AREA_WIDTH * 4)
        } else width

        return Pair(startBounds, endBounds)
    }

    fun setOnLayerScroll(op: (Int) -> Unit) {
        onScrollProperty.set(op)
    }

    fun setOnDragStarted(op: () -> Unit) {
        onDragStartedProperty.set(op)
    }
}

fun EventTarget.verse_markers_layer(
    op: VerseMarkersLayer.() -> Unit = {}
): VerseMarkersLayer {
    val markerLayer = VerseMarkersLayer()
    opcr(this, markerLayer, op)
    return markerLayer
}

class NarrationMarkerChangedEvent(val index: Int, val delta: Int) : FXEvent()

class NarrationMovingMarkerEvent(val index: Int) : FXEvent()