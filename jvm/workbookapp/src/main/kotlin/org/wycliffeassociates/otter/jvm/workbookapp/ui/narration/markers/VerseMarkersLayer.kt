package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener

/**
 * This is the offset of the marker line relative
 * to the draggable area. In other words,
 * it's a half the length of marker draggable area.
 */
private const val MARKER_OFFSET = (MARKER_AREA_WIDTH / 2).toInt()

class VerseMarkersLayer : StackPane() {

    val isRecordingProperty = SimpleBooleanProperty()
    val markers = observableListOf<VerseMarker>()
    val rightOffset = SimpleIntegerProperty(0) // corresponds to pixels from incoming audio

    // gets the last verseMarker's location and sets that to the total frames
    private val totalFramesProperty = markers.integerBinding { it[it.size - 1].location }
    private val onScrollProperty = SimpleObjectProperty<(Double) -> Unit>()

    private val layerWidthProperty = SimpleDoubleProperty()

    private val markersTotalWidthProperty = totalFramesProperty.doubleBinding { it?.let {
        framesToPixels(it.toInt()).toDouble() } ?: 1.0
    }

    // Stores the relative start/end positions in the audio file that are being shown.
    val scrollBarPositionProperty = SimpleDoubleProperty(0.0)
    var verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()


    init {

        maxWidth = 1920.0 // TODO: fix to account for arrows of scrollbar

        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        layerWidthProperty.bind(widthProperty())

        region {
            addClass("verse-marker__play-head")
        }

        scrollpane {
            setOnLayerScroll {
                hvalue += it / markersTotalWidthProperty.value
            }

            hbox {
                isFitToHeight = true
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

                hvalueProperty().addListener { _, _, newValue ->
                    val contentWidth = markersTotalWidthProperty.value // Get the width of the content
                    val scrollbarPositionRatio : Double = hvalueProperty().value * contentWidth

                    scrollBarPositionProperty.set(scrollbarPositionRatio)
                }
                totalFramesProperty.addListener { _, old, new ->
                    this.minWidth = framesToPixels(totalFramesProperty.value).toDouble()
                    this.maxWidth = framesToPixels(totalFramesProperty.value).toDouble()
                }
                anchorpane {
                    totalFramesProperty.addListener { _, old, new ->
                        this.minWidth = framesToPixels(totalFramesProperty.value).toDouble()
                        this.maxWidth = framesToPixels(totalFramesProperty.value).toDouble()
                    }


                    bindChildren(verseMarkersControls) { verseMarkerControl ->

                        val prevVerse = getPrevVerse(verseMarkerControl.verseProperty.value)
                        val nextVerse = getNextVerse(verseMarkerControl.verseProperty.value)
                        val previousMarkerPosition = framesToPixels(prevVerse.location)
                        val nextMarkerPosition = framesToPixels(nextVerse.location)

                        val currentMarkerPosition = verseMarkerControl.verseProperty.value.location
                        val endPosInPixels = framesToPixels(currentMarkerPosition)

                        verseMarkerControl.apply {

                            val dragTarget = dragAreaProperty.value

                            var delta = 0.0
                            var oldPos = 0.0

                            dragTarget.setOnMousePressed { event ->
                                if (!canBeMovedProperty.value) return@setOnMousePressed
                                delta = 0.0
                                oldPos = AnchorPane.getLeftAnchor(this)

                                event.consume()
                            }

                            dragTarget.setOnMouseDragged { event ->
                                if (!canBeMovedProperty.value) return@setOnMouseDragged

                                val point = localToParent(event.x, event.y)
                                val currentPos = point.x

                                if (currentPos.toInt() in (previousMarkerPosition + 1) .. nextMarkerPosition) {
                                    delta = currentPos - oldPos
                                    AnchorPane.setLeftAnchor(this, currentPos - MARKER_OFFSET)
                                }

                                event.consume()
                            }

                            dragTarget.setOnMouseReleased { event ->
                                if (delta != 0.0) {
                                    delta -= MARKER_OFFSET
                                    val frameDelta = pixelsToFrames(delta)
                                    // TODO: I need to update the relative and actual location of the marker.
                                    // Pretty sure that this is done by firing a VerseMarkerAction, but not sure
                                    println("frameDelta: ${frameDelta}, delta: ${delta}")
                                    FX.eventbus.fire(NarrationMarkerChangedEvent(markers.indexOf(verseMarkerControl.verseProperty.value), frameDelta))
                                }
                                event.consume()
                            }

                            anchorpaneConstraints {
                                topAnchor = 0.0
                                bottomAnchor = 0.0
                                leftAnchor = endPosInPixels - MARKER_OFFSET
                            }

                        }
                    }
                }
            }
        }
    }

    private fun setOnLayerScroll(op: (Double) -> Unit) {
        onScrollProperty.set(op)
    }

    private fun getVerseLabel(verse: VerseMarker): String {
        val index = markers.indexOf(verse)
        return (index + 1).toString()
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

class NarrationMarkerChangedEvent(val index: Int, val delta: Int) : FXEvent()