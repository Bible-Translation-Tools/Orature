package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import io.reactivex.Observable
import javafx.animation.AnimationTimer
import javafx.geometry.Rectangle2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val verseMarkerViewModel: VerseMarkerViewModel by inject()
    var imageView = ImageView()

    init {
        Observable.fromCallable {
            val width = Screen.getMainScreen().platformWidth
            val height = Screen.getMainScreen().platformHeight
            val img = WritableImage(width * 10 + (width), height * 3)
            for (x in 0 until (width / 2)) {
                for (y in 0 until height) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
                for (y in height until (height * 2)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGRAY)
                }
                for (y in (height * 2) until (height * 3)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
            }
            for (x in (width / 2) until ((width / 2) + width * 10)) {
                for (y in 0 until height) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
                for (y in height until (height * 2)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTBLUE)
                }
                for (y in (height * 2) until (height * 3)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
            }
            for (x in ((width / 2) + width * 10) until (((width / 2) + width * 10) + width / 2)) {
                for (y in 0 until height) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
                for (y in height until (height * 2)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGRAY)
                }
                for (y in (height * 2) until (height * 3)) {
                    img.pixelWriter.setColor(x, y, Color.LIGHTGREEN)
                }
            }
            img as Image
        }.subscribe { image ->
            imageView.imageProperty().set(image)
        }
    }

    override val root = region {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS


        imageView.fitHeightProperty().bind(this.heightProperty())

        val ht = Screen.getMainScreen().platformHeight
        val wd = Screen.getMainScreen().platformWidth

        imageView.viewport = Rectangle2D(360.0, ht.toDouble(), wd.toDouble(), ht.toDouble())

        val at = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                val player = verseMarkerViewModel.audioPlayer
                val padding = Screen.getMainScreen().platformWidth / 2.0
                val width = imageView.image.width - (padding * 2)
                val pos =
                    (player.getAbsoluteLocationInFrames() / player.getAbsoluteDurationInFrames().toDouble()) * width
                imageView.viewport = Rectangle2D(pos, ht.toDouble(), wd.toDouble(), ht.toDouble())
            }
        }.start()

        stackpane {
            fitToParentWidth()
            fitToParentHeight()
            add(imageView)
            add(PlaceMarkerLayer())
        }
    }
}
