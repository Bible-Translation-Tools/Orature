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
                val relativePos = absoluteFrameToRelative(verse)
                val posInPixels = framesToPixels(relativePos)
                val verseLabel = getVerseLabel(verse)

                verseProperty.set(verse)
                verseIndexProperty.set(markers.indexOf(verse))
                positionProperty.set(posInPixels)
                labelProperty.set(verseLabel)

                isRecordingProperty.bind(this@VerseMarkersLayer.isRecordingProperty)

                var delta = 0.0
                var pos = 0.0

                setOnDragDetected { event ->
                    if (!canBeMovedProperty.value) return@setOnDragDetected

                    val point = localToParent(event.x, event.y)
                    delta = 0.0
                    pos = point.x

                    println("onDragDetected: ${point.x}")
                    println("Pos: ${pos}")

                    event.consume()
                }

                setOnMouseDragged { event ->
                    if (!canBeMovedProperty.value) return@setOnMouseDragged

                    val point = localToParent(event.x, event.y)
                    println("onMouseDragged: ${point.x}")
                    println("pos: $pos")

                    delta = point.x - pos

                    println("delta: $delta")

                    positionProperty.set(point.x.toInt())

                    event.consume()
                }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }
    }

    private fun absoluteFrameToRelative(verse: VerseNode): Int {
        val index = markers.indexOf(verse)
        val prevVerses = markers.slice(0 until index)

        return prevVerses.sumOf { it.end - it.start }
    }

    private fun getVerseLabel(verse: VerseNode): String {
        val index = markers.indexOf(verse)
        return (index + 1).toString()
    }
}