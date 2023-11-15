package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.event.EventTarget
import javafx.scene.layout.BorderPane
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import java.lang.IllegalStateException

/**
 * This is the offset of the marker line relative
 * to the draggable area. In other words,
 * it's a half the length of marker draggable area.
 */
private const val MARKER_OFFSET = (MARKER_AREA_WIDTH / 2).toInt()

class VerseMarkersLayer : BorderPane() {
    private val logger = LoggerFactory.getLogger(VerseMarkersLayer::class.java)

    val isRecordingProperty = SimpleBooleanProperty()
    val markers = observableListOf<VerseMarker>()

    val verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()
    private val onScrollProperty = SimpleObjectProperty<(Int) -> Unit>()


    init {
        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        var scrollDelta = 0.0
        var scrollOldPos = 0.0

        setOnMousePressed { event ->
            val point = localToParent(event.x, event.y)
            scrollOldPos = point.x
            scrollDelta = 0.0
        }

        setOnMouseDragged { event ->
            val point = localToParent(event.x, event.y)
            scrollDelta = scrollOldPos - point.x
            val frameDelta = pixelsToFrames(scrollDelta, width = this@VerseMarkersLayer.width.toInt())
            onScrollProperty.value?.invoke(frameDelta)
            scrollDelta = 0.0
            scrollOldPos = point.x
        }


        bindChildren(verseMarkersControls) { verseMarkerControl ->

            val prevVerse = getPrevVerse(verseMarkerControl.verseProperty.value)
            val nextVerse = getNextVerse(verseMarkerControl.verseProperty.value)

            verseMarkerControl.apply {

                val dragTarget = dragAreaProperty.value

                var delta = 0.0
                var oldPos = 0.0

                minHeightProperty().bind(this@VerseMarkersLayer.heightProperty())
                prefHeightProperty().bind(this@VerseMarkersLayer.heightProperty())

                dragTarget.setOnMousePressed { event ->
                    userIsDraggingProperty.set(true)
                    if (!canBeMovedProperty.value) return@setOnMousePressed
                    delta = 0.0
                    oldPos = layoutX

                    event.consume()
                }

                dragTarget.setOnMouseDragged { event ->
                    userIsDraggingProperty.set(true)
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    try {
                        val point = localToParent(event.x, event.y)
                        val (start, end) = verseBoundaries(verseIndexProperty.value, this.width)
                        val currentPos = point.x.coerceIn(start..end)

                        delta = currentPos - oldPos

                        layoutX = currentPos
                    } catch (e: Exception) {
                        // This can prevent attempting to move a marker that was originally created too close together
                        // if the user spammed next verse while recording. Moving surrounding markers around will allow
                        // for this marker to get enough space to move around.
                        logger.error("Tried to move a marker, but aborted", e)
                    }

                    event.consume()
                }



                dragTarget.setOnMouseReleased { event ->
                    if (delta != 0.0) {
                        // delta -= MARKER_OFFSET
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

    private fun verseBoundaries(verseIndex: Int, boundingWidth: Double): Pair<Double, Double> {
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

    private fun getPrevVerse(verse: AudioMarker): AudioMarker {
        val currentIndex = markers.indexOf(verse)
        return markers.getOrNull(currentIndex - 1) ?: verse
    }

    private fun getNextVerse(verse: AudioMarker): AudioMarker {
        val currentIndex = markers.indexOf(verse)
        return markers.getOrNull(currentIndex + 1) ?: verse
    }

    fun setOnLayerScroll(op: (Int) -> Unit) {
        onScrollProperty.set(op)
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