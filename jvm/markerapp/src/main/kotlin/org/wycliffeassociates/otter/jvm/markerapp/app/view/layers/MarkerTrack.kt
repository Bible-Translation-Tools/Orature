package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.collections.FXCollections
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerTrack(val viewModel: VerseMarkerViewModel) : Region() {

    var scale: Double = 1.0
    val markers = FXCollections.observableArrayList<ChunkMarker>()
    val rectangles = FXCollections.observableArrayList<Rectangle>()

    init {
        styleClass.add("vm-marker-track")

        widthProperty().onChange {
            scale = viewModel.audioPlayer.getAbsoluteDurationInFrames() / it
            resetMakers()
        }

        viewModel.markers.markerCountProperty.onChangeAndDoNow {
            resetMakers()
        }

        markers.onChangeAndDoNow {
            children.clear()
            children.addAll(rectangles)
            children.addAll(markers)
        }

        viewModel.positionProperty.onChangeAndDoNow {
            val x = it?.toDouble() ?: 0.0
            scrollTo(x)
        }

        // This allows for aligning the track when it first sets up
        // as the viewmodel position onchange event will fire before
        // this node is assigned a parent, whose width is needed
        parentProperty().onChange {
            (it as? Region)?.let {
                it.widthProperty().onChange {
                    scrollTo(viewModel.positionProperty.value)
                }
            }
        }
    }
    private fun resetMakers() {
        markers.clear()
        markers.setAll(
            viewModel.markers.cues.mapIndexed { index, cue ->
                if (index > 0) {
                    val rectWidth = (cue.location - viewModel.markers.cues[index - 1].location) / scale
                    rectangles.add(
                        Rectangle(rectWidth, height).apply {
                            xProperty().set(viewModel.markers.cues[index - 1].location / scale.toDouble())
                            fill = if (index % 2 == 0) {
                                Paint.valueOf("#1edd7633")
                            } else {
                                Paint.valueOf("#015ad933")
                            }
                        }
                    )
                } else {
                    val rectWidth = (viewModel.audioPlayer.getAbsoluteDurationInFrames() - cue.location) / scale
                    rectangles.add(
                        Rectangle(rectWidth, height).apply {
                            xProperty().set(
                                viewModel.audioPlayer.getAbsoluteDurationInFrames() - cue.location / scale.toDouble()
                            )
                            fill = if (index % 2 == 0) {
                                Paint.valueOf("#1edd7633")
                            } else {
                                Paint.valueOf("#015ad933")
                            }
                        }
                    )
                }
                ChunkMarker().apply {
                    markerNumberProperty.set(cue.label)
                    val x = cue.location / scale.toDouble()
                    translateXProperty().set(x)
                }
            }
        )
    }

    fun scrollTo(x: Double) {
        val parentWidth = (parent as? Region)?.widthProperty()?.get() ?: width
        val scaleFactor = parentWidth / Screen.getMainScreen().platformWidth.toDouble()

        // this formula was computed by plotting points of (parent width, offset)
        val trackOffset = (parentWidth * 1.355) - 1231
        scaleXProperty().set(scaleFactor)
        translateXProperty().set(trackOffset - x * scaleFactor)
    }
}
