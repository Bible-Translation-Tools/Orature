package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.property.DoubleProperty
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.markerapp.app.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerViewBackground
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.WaveformOverlay
import tornadofx.add
import tornadofx.hgrow
import tornadofx.vgrow

class ScrollingWaveform(
    positionProperty: DoubleProperty,
    onPlaceMarker: () -> Unit,
    onWaveformClicked: () -> Unit,
    onWaveformDragReleased: (Double) -> Unit,
    topTrack: Node? = null,
    bottomTrack: Node? = null
) : StackPane() {

    private val waveformFrame: WaveformFrame

    init {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        styleClass.add("vm-waveform-container")

        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        add(MarkerViewBackground())
        waveformFrame = WaveformFrame(
            topTrack,
            bottomTrack
        ).apply {
            framePositionProperty.bind(positionProperty)
            onWaveformClicked { onWaveformClicked() }
            onWaveformDragReleased {
                onWaveformDragReleased(it)
            }
        }
        add(waveformFrame)
        add(WaveformOverlay().apply { playbackPositionProperty.bind(positionProperty) })
        add(PlaceMarkerLayer().apply { onPlaceMarkerAction { onPlaceMarker() } })
    }

    fun freeImages() {
        waveformFrame.freeImages()
    }

    fun addHighlights(highlights: List<MarkerHighlightState>) {
        waveformFrame.addHighlights(highlights)
    }

    fun addWaveformImage(image: Image) {
        waveformFrame.addImage(image)
    }
}
