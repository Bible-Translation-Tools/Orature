package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.AnchorPane
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class VerseMarkersLayer : AnchorPane() {

    val isRecordingProperty = SimpleBooleanProperty()

    val markers = observableListOf<VerseNode>()
    val totalFrames = markers.integerBinding {
        it.sumOf { verse -> verse.end - verse.start }
    }

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
                startPositionProperty.set(startPosInPixels)
                endPositionProperty.set(endPosInPixels)
                labelProperty.set(verseLabel)

                isRecordingProperty.bind(this@VerseMarkersLayer.isRecordingProperty)

                val dragTarget = dragAreaProperty.value

                var delta = 0
                var pos = 0

                dragTarget.setOnDragDetected { event ->
                    if (!canBeMovedProperty.value) return@setOnDragDetected

                    val point = localToParent(event.x, event.y)
                    delta = 0
                    pos = point.x.toInt()

                    event.consume()
                }

                dragTarget.setOnMouseDragged { event ->
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    val point = localToParent(event.x, event.y)
                    val currentPos = point.x.toInt()
                    delta = currentPos - pos

                    if (currentPos in prevStartPosInPixels..endPosInPixels) {
                        startPositionProperty.set(currentPos)
                    }

                    event.consume()
                }

                dragTarget.setOnMouseReleased { event ->
                    if (delta > 0) {
                        println(startPositionProperty.value)
                    }
                    event.consume()
                }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    bottomAnchor = 0.0
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