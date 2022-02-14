package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.ScrollingWaveformSkin

open class ScrollingWaveform() : Control() {
    val positionProperty = SimpleDoubleProperty(0.0)

    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}

    fun addWaveformImage(image: Image) {
        (skin as ScrollingWaveformSkin).addWaveformImage(image)
    }

    fun freeImages() {
        (skin as ScrollingWaveformSkin).freeImages()
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScrollingWaveformSkin(this)
    }
}
