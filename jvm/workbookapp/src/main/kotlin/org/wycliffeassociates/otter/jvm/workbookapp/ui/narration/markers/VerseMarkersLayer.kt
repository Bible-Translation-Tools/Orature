package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import com.sun.glass.ui.Screen
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import javafx.util.Duration
import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
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

    // TODO: Examine if I need to update this because framesToPixels uses the total screen size, and does not max out at 1920.
    private val markersTotalWidthProperty = totalFramesProperty.doubleBinding { it?.let {
        framesToPixels(it.toInt()).toDouble() } ?: 1.0
    }

//    val markersTotalWidthProperty: DoubleBinding = Bindings.createDoubleBinding(
//        { framesToPixels(totalFramesProperty.value).toDouble() + rightOffset.value },
//        totalFramesProperty, rightOffset
//    )

    val verseMarkersControlsInView: ObservableList<VerseMarkerControl> = observableListOf()

    init {

        // Manually shifts all verse markers to the left
        rightOffset.addListener{_, old, new ->
            println("iterating over verses: ${verseMarkersControlsInView.size}")
            verseMarkersControlsInView.forEach { verseMarkerControl ->
                if(new == 0) {
                    AnchorPane.setLeftAnchor(verseMarkerControl,
                        framesToPixels(verseMarkerControl.verseProperty.value.location).toDouble() - MARKER_OFFSET
                    )
                } else {
                    val currentLeftAnchor = AnchorPane.getLeftAnchor(verseMarkerControl) ?: 0.0
                    val newLeftAnchor = currentLeftAnchor - (maxOf(0, new.toInt() - old.toInt()))
                    AnchorPane.setLeftAnchor(verseMarkerControl, newLeftAnchor)
                }

            }
        }

        //maxWidth = 1920.0 // TODO: fix to account for arrows of scrollbar

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
                    println("point: ${point}, scrollDelta: ${scrollDelta}")
                    onScrollProperty.value?.invoke(scrollDelta)
                }



                anchorpane {
//                    markersTotalWidthProperty.addListener { _ ->
//                        println("markersTotalWidthProperty has changed: ${markersTotalWidthProperty.value}")
//                        maxWidthProperty().set(markersTotalWidthProperty.value)
//
//                        prefWidthProperty().set(markersTotalWidthProperty.value)
//                    }

                    bindChildren(markers) { verse ->

                        // TODO: skip over verses that are not in range here

                        val verseLabel = getVerseLabel(verse)
                        val prevVerse = getPrevVerse(verse)
                        val nextVerse = getNextVerse(verse)
                        val previousMarkerPosition = framesToPixels(prevVerse.location)
                        val nextMarkerPosition = framesToPixels(nextVerse.location)

                        val currentMarkerPosition = verse.location
                        val endPosInPixels = framesToPixels(currentMarkerPosition)

                        VerseMarkerControl().apply {

                            // add node to list
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
                                    FX.eventbus.fire(NarrationMarkerChangedEvent(markers.indexOf(verse), frameDelta))
                                }
                                event.consume()
                            }

                            anchorpaneConstraints {
                                topAnchor = 0.0
                                bottomAnchor = 0.0
                                leftAnchor = endPosInPixels - MARKER_OFFSET//endPosInPixels - MARKER_OFFSET
                            }

                            if(!verseMarkersControlsInView.contains(this)) {
                                println("adding verse to list ${this.labelProperty.value}")
                                verseMarkersControlsInView.add(this)
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

    private fun absolutePositionToRelative(verse: VerseMarker): Pair<Int, Int> {
        val index = markers.indexOf(verse)
        val prevVerses = markers.slice(0 until index)
        val relStart = prevVerses.sumOf { it.end - it.start }
        val relEnd = relStart + (verse.end - verse.start)

        return Pair(relStart, relEnd)
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