package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.scene.control.ScrollPane

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
    private val layerHeightProperty = SimpleDoubleProperty(0.0)
    private lateinit var scrollLayer: ScrollPane

    private val markersTotalWidthProperty = totalFramesProperty.doubleBinding { it?.let {
        framesToPixels(it.toInt()).toDouble() } ?: 1.0
    }

    val scrollBarPositionProperty = SimpleDoubleProperty(0.0)
    var verseMarkersControls: ObservableList<VerseMarkerControl> = observableListOf()
    val MAX_SCREEN_WIDTH = 1920.0
    val SCROLLBAR_HEIGHT = 50

    init {
        maxWidth = MAX_SCREEN_WIDTH

        tryImportStylesheet("/css/verse-markers-layer.css")

        addClass("verse-markers-layer")

        layerWidthProperty.bind(widthProperty())
        layerHeightProperty.bind(heightProperty())

        region {
            addClass("verse-marker__play-head")
        }

        scrollpane {
            scrollLayer = this

            setOnLayerScroll {
                hvalue += it / markersTotalWidthProperty.value
            }

            hbox {
                isFitToHeight = true

                hvalueProperty().addListener { _, _, newValue ->
                    val contentWidth = markersTotalWidthProperty.value // Get the width of the content
                    val scrollbarPositionRatio : Double = hvalueProperty().value * contentWidth

                    scrollBarPositionProperty.set(scrollbarPositionRatio)
                }

                anchorpane {
                    totalFramesProperty.addListener { _, old, new ->
                        this.minWidth = framesToPixels(totalFramesProperty.value).toDouble()
                        this.maxWidth = framesToPixels(totalFramesProperty.value).toDouble()
                    }
                }
            }
        }


        // For the actual verseMarkers
        hbox {
            maxWidth = 1920.0
            maxHeight = 100.0
            var dragStartX: Double? = null

            // Makes room for the scrollbar, so that this layer in the stackPane does not block it
            this.maxHeightProperty().bind(layerHeightProperty.minus(SCROLLBAR_HEIGHT))

            setOnMousePressed { event ->
                dragStartX = event.x
            }

            setOnMouseDragged { event ->
                dragStartX?.let { startX ->
                    val deltaX = startX - event.x
                    dragStartX = event.x
                    // Update the scrollPane's hvalue based on the dragging distance
                    scrollLayer.hvalue += deltaX / markersTotalWidthProperty.value
                }
            }

            bindChildren(verseMarkersControls) { verseMarkerControl ->
                verseMarkerControl
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