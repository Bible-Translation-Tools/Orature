package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import tornadofx.*

class VerseMarkersLayer : HBox() {

    val isRecordingProperty = SimpleBooleanProperty()

    val markers = observableListOf<VerseNode>()
    val totalFrames = markers.integerBinding {
        it.sumOf { verse -> verse.end - verse.start }
    }

    init {
        style {
            borderColor += box(Color.RED)
        }

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