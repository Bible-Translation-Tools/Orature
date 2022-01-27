package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.NodeOrientation
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.markerapp.app.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerViewBackground
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.WaveformOverlay
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.ScrollingWaveform
import tornadofx.add
import tornadofx.hgrow
import tornadofx.vgrow

open class ScrollingWaveformSkin(control: ScrollingWaveform) : SkinBase<ScrollingWaveform>(control) {

    protected lateinit var waveformFrame: WaveformFrame

    init {
        initialize()
    }

    open fun initialize() {
        val root = StackPane().apply {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            styleClass.add("vm-waveform-container")

            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

            add(MarkerViewBackground())
            waveformFrame = WaveformFrame().apply {
                framePositionProperty.bind(skinnable.positionProperty)
                onWaveformClicked { skinnable.onWaveformClicked() }
                onWaveformDragReleased {
                    skinnable.onWaveformDragReleased(it)
                }
            }
            add(waveformFrame)
            add(WaveformOverlay().apply { playbackPositionProperty.bind(skinnable.positionProperty) })
        }
        children.add(root)
    }

    fun freeImages() {
        waveformFrame.freeImages()
    }

    fun addWaveformImage(image: Image) {
        waveformFrame.addImage(image)
    }
}

class MarkerPlacementWaveformSkin(val control: MarkerPlacementWaveform) : ScrollingWaveformSkin(control) {

    fun addHighlights(highlights: List<MarkerHighlightState>) {
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
        }
        children.add(root)
    }
}
