package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.NodeOrientation
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.markerapp.app.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerViewBackground
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.WaveformOverlay
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.add
import tornadofx.hgrow
import tornadofx.vgrow

class MarkerPlacementWaveformSkin(val control: MarkerPlacementWaveform) : ScrollingWaveformSkin(control) {

    private fun addHighlights(highlights: List<MarkerHighlightState>) {
        waveformFrame.clearHighlights()
        waveformFrame.addHighlights(highlights)
    }

    override fun initialize() {
        val root = StackPane().apply {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            styleClass.add("vm-waveform-container")

            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

            add(MarkerViewBackground())
            waveformFrame = WaveformFrame(
                (skinnable as MarkerPlacementWaveform).topTrack,
            ).apply {
                framePositionProperty.bind(skinnable.positionProperty)
                onWaveformClicked { skinnable.onWaveformClicked() }
                onWaveformDragReleased {
                    skinnable.onWaveformDragReleased(it)
                }
            }
            add(waveformFrame)
            add(WaveformOverlay().apply { playbackPositionProperty.bind(skinnable.positionProperty) })
            add(PlaceMarkerLayer().apply { onPlaceMarkerAction { control.onPlaceMarker() } })

            (skinnable as MarkerPlacementWaveform).markerStateProperty.onChangeAndDoNow { markers ->
                markers?.let { markers ->
                    addHighlights(markers.highlightState)
                }
            }

        }
        children.add(root)
    }
}
