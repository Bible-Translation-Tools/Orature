package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.AnchorPane
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

private const val MARKER_OFFSET = (MARKER_AREA_WIDTH / 2).toInt()

class VerseMarkersLayer : AnchorPane() {

    val isRecordingProperty = SimpleBooleanProperty()
    val markers = observableListOf<VerseNode>()

    init {
        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        //isMouseTransparent = true

        bindChildren(markers) { verse ->
            VerseMarker().apply {
                val (relStart, relEnd) = absolutePositionToRelative(verse)
                val startPosInPixels = framesToPixels(relStart)
                val endPosInPixels = framesToPixels(relEnd)

                val verseLabel = getVerseLabel(verse)
                val prevVerse = getPrevVerse(verse)
                val (prevRelStart, _) = absolutePositionToRelative(prevVerse)
                val prevStartPosInPixels = framesToPixels(prevRelStart)

                verseProperty.set(verse)
                verseIndexProperty.set(markers.indexOf(verse))
                labelProperty.set(verseLabel)

                isRecordingProperty.bind(this@VerseMarkersLayer.isRecordingProperty)

                val dragTarget = dragAreaProperty.value

                var delta = 0
                var oldPos = 0

                dragTarget.setOnMousePressed { event ->
                    if (!canBeMovedProperty.value) return@setOnMousePressed

                    delta = 0
                    oldPos = getLeftAnchor(this).toInt()

                    event.consume()
                }

                dragTarget.setOnMouseDragged { event ->
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    val point = localToParent(event.x, event.y)
                    val currentPos = point.x.toInt()
                    delta = currentPos - oldPos

                    if (currentPos in prevStartPosInPixels..endPosInPixels) {
                        anchorpaneConstraints {
                            leftAnchor = currentPos - MARKER_OFFSET
                        }
                    }

                    event.consume()
                }

                dragTarget.setOnMouseReleased { event ->
                    if (delta != 0) {
                        delta -= MARKER_OFFSET
                        val frameDelta = pixelsToFrames(delta.toDouble())
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