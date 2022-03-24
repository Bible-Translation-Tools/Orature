package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.NodeOrientation
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerViewBackground
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.WaveformOverlay
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
                onWaveformClicked(skinnable.onWaveformClicked)
                onWaveformDragReleased(skinnable.onWaveformDragReleased)
                onRewind(skinnable.onRewind)
                onFastForward(skinnable.onFastForward)
                onToggleMedia(skinnable.onToggleMedia)
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
