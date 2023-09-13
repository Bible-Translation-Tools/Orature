package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.event.EventTarget
import javafx.scene.layout.BorderPane

/**
 * This is the offset of the marker line relative
 * to the draggable area. In other words,
 * it's a half the length of marker draggable area.
 */
private const val MARKER_OFFSET = (MARKER_AREA_WIDTH / 2).toInt()

class VerseMarkersLayer : BorderPane() {

    val isRecordingProperty = SimpleBooleanProperty()
    val markers = observableListOf<VerseMarker>()

    val verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()


    init {
        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

//            setOnMousePressed { event ->
//                val point = localToParent(event.x, event.y)
//                scrollOldPos = point.x
//                scrollDelta = 0.0
//            }
//
//            setOnMouseDragged { event ->
//                val point = localToParent(event.x, event.y)
//                scrollDelta = scrollOldPos - point.x
//                onScrollProperty.value?.invoke(scrollDelta)
//            }


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

                            val point = localToParent(event.x, event.y)
                            val currentPos = point.x

                            delta = currentPos - oldPos
                            layoutX = currentPos

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

    private fun getPrevVerse(verse: VerseMarker): VerseMarker {
        val currentIndex = markers.indexOf(verse)
        return markers.getOrNull(currentIndex - 1) ?: verse
    }

    private fun getNextVerse(verse: VerseMarker): VerseMarker {
        val currentIndex = markers.indexOf(verse)
        return markers.getOrNull(currentIndex + 1) ?: verse
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