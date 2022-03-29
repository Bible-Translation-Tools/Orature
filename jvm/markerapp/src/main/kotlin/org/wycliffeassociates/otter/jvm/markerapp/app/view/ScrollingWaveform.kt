package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.SeekSpeed

open class ScrollingWaveform : Control() {
    val positionProperty = SimpleDoubleProperty(0.0)
    val playerProperty = SimpleObjectProperty<IAudioPlayer>()

    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}
    var onRewind: ((SeekSpeed) -> Unit) = {}
    var onFastForward: ((SeekSpeed) -> Unit) = {}
    var onToggleMedia: () -> Unit = {}

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
