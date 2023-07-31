package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.AnchorPane
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

/**
 * This is the offset of the marker line relative
 * to the draggable area. In other words,
 * it's a half the length of marker draggable area.
 */
private const val MARKER_OFFSET = (MARKER_AREA_WIDTH / 2).toInt()

class VerseMarkersLayer : AnchorPane() {

    val isRecordingProperty = SimpleBooleanProperty()
    val markers = observableListOf<VerseNode>()

    private val totalFramesProperty = markers.integerBinding { it.sumOf { verse -> verse.end - verse.start } }
    private val onScrollProperty = SimpleObjectProperty<(Double) -> Unit>()

    init {
        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        var scrollOldPos = 0.0
        var scrollDelta: Double

        setOnMousePressed { event ->
            val point = localToParent(event.x, event.y)

            scrollOldPos = point.x
            scrollDelta = 0.0
        }

        setOnMouseDragged { event ->
            val point = localToParent(event.x, event.y)
            scrollDelta = scrollOldPos - point.x

            onScrollProperty.value?.invoke(scrollDelta)
        }

        prefWidthProperty().bind(
            totalFramesProperty.doubleBinding { it?.let { framesToPixels(it.toInt()).toDouble() } ?: 1.0 }
        )

        bindChildren(markers) { verse ->
            val (relStart, relEnd) = absolutePositionToRelative(verse)
            val startPosInPixels = framesToPixels(relStart)
            val endPosInPixels = framesToPixels(relEnd)

            val verseLabel = getVerseLabel(verse)
            val prevVerse = getPrevVerse(verse)
            val (prevRelStart, _) = absolutePositionToRelative(prevVerse)
            val prevStartPosInPixels = framesToPixels(prevRelStart)

            VerseMarker().apply {
                verseProperty.set(verse)
                verseIndexProperty.set(markers.indexOf(verse))
                labelProperty.set(verseLabel)

                isRecordingProperty.bind(this@VerseMarkersLayer.isRecordingProperty)

                val dragTarget = dragAreaProperty.value

                var delta = 0.0
                var oldPos = 0.0

                dragTarget.setOnMousePressed { event ->
                    if (!canBeMovedProperty.value) return@setOnMousePressed

                    delta = 0.0
                    oldPos = getLeftAnchor(this)

                    event.consume()
                }

                dragTarget.setOnMouseDragged { event ->
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    val point = localToParent(event.x, event.y)
                    val currentPos = point.x
                    val prevStartThreshold = prevStartPosInPixels + width + MARKER_OFFSET
                    val endPosThreshold = endPosInPixels - width - MARKER_OFFSET

                    if (currentPos in prevStartThreshold..endPosThreshold) {
                        delta = currentPos - oldPos
                        setLeftAnchor(this, currentPos - MARKER_OFFSET)
                    }

                    event.consume()
                }

                dragTarget.setOnMouseReleased { event ->
                    if (delta != 0.0) {
                        delta -= MARKER_OFFSET
                        val frameDelta = pixelsToFrames(delta)
                        FX.eventbus.fire(NarrationMarkerChangedEvent(markers.indexOf(verse), frameDelta))
                    }
                    event.consume()
                }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    bottomAnchor = 0.0
                    leftAnchor = startPosInPixels - MARKER_OFFSET
                }
            }
        }
    }

    fun setOnScroll(op: (Double) -> Unit) {
        onScrollProperty.set(op)
    }

    private fun absolutePositionToRelative(verse: VerseNode): Pair<Int, Int> {
        val index = markers.indexOf(verse)
        val prevVerses = markers.slice(0 until index)
        val relStart = prevVerses.sumOf { it.end - it.start }
        val relEnd = relStart + (verse.end - verse.start)

        return Pair(relStart, relEnd)
    }

    private fun getVerseLabel(verse: VerseNode): String {
        val index = markers.indexOf(verse)
        return (index + 1).toString()
    }

    private fun getPrevVerse(verse: VerseNode): VerseNode {
        val currentIndex = markers.indexOf(verse)
        return markers.getOrNull(currentIndex - 1) ?: verse
    }
}

class NarrationMarkerChangedEvent(val index: Int, val delta: Int) : FXEvent()