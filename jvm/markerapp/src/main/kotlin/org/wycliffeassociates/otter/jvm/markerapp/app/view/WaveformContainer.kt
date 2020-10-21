package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.Timecode
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*
import java.lang.Math.floor

class WaveformContainer : Fragment() {

    val verseMarkerViewModel: VerseMarkerViewModel by inject()
    var imageView = ImageView().apply { style { backgroundColor += Paint.valueOf("#0a337333") } }
    val playedOverlay = Rectangle()
    val positionProperty = SimpleDoubleProperty(0.0)
    val timecode: Timecode
    val markerTrack: Region
    val timecodeImageView = ImageView()

    init {

        val width = Screen.getMainScreen().platformWidth
        val height = Screen.getMainScreen().platformHeight

        val imageWidth =
            (44100 * 5 / width.toDouble()) * (verseMarkerViewModel.audioPlayer.getAbsoluteDurationMs() / 1000.0)

        markerTrack = MarkerTrack(verseMarkerViewModel, imageWidth, 50.0)//TimecodeRegion(verseMarkerViewModel.audioPlayer.getAbsoluteDurationMs(), imageWidth.toInt(), 40)
        timecode = Timecode(floor(imageWidth), 50.0)
        timecodeImageView.image = timecode.drawTimecode(verseMarkerViewModel.audioPlayer.getAbsoluteDurationMs())


        WaveformImageBuilder(
            paddingColor = Color.web("#0a337333"),
            wavColor = Color.web("#0A337390"),
            background = Color.web("#F7FAFF")
        ).build(
            verseMarkerViewModel.audioPlayer.getAudioReader()!!,
            fitToAudioMax = false,
            width = imageWidth.toInt(),
            height = height
        ).subscribe { image ->
            imageView.imageProperty().set(image)
        }
    }

    override val root = borderpane {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        val ht = Screen.getMainScreen().platformHeight
        val wd = Screen.getMainScreen().platformWidth

        val at = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (imageView != null && imageView.image != null) {
                    val player = verseMarkerViewModel.audioPlayer
                    val padding = Screen.getMainScreen().platformWidth / 2
                    val width = imageView.image.width
                    val pos =
                        (player.getAbsoluteLocationInFrames() / player.getAbsoluteDurationInFrames().toDouble()) * width
                    positionProperty.set(pos)
                    imageView.viewport = Rectangle2D(pos - padding, 0.0, wd.toDouble(), ht.toDouble())
                    timecodeImageView.viewport =
                        Rectangle2D(pos - padding, 0.0, wd.toDouble(), timecodeImageView.image.height)
                    markerTrack.translateXProperty().set(-pos + this@borderpane.widthProperty().get() / 2)
                }
            }
        }.start()

        top {
            region {
                prefHeight = 40.0
                style {
                    backgroundColor += Paint.valueOf("#a2b2cd")
                }

                stackpane {
                    add(markerTrack)
                }
            }
        }

        center {
            region {

                timecodeImageView.fitWidthProperty().bind(this.widthProperty())
                imageView.fitHeightProperty().bind(this.heightProperty())
                imageView.fitWidthProperty().bind(this.widthProperty())

                stackpane {
                    alignment = Pos.CENTER

                    fitToParentWidth()
                    fitToParentHeight()
                    add(imageView)
                    add(
                        playedOverlay.apply {
                            heightProperty().bind(this@region.heightProperty())
                            widthProperty().bind(
                                Bindings.min(
                                    positionProperty.times(this@region.widthProperty() / Screen.getMainScreen().width),
                                    this@region.widthProperty().divide(2)
                                )
                            )
                            translateXProperty().bind(-widthProperty() / 2)
                            style {
                                fillProperty().set(Paint.valueOf("#015ad966"))
                            }
                        }
                    )
                    style {
                        backgroundColor += Paint.valueOf("#c8d2e3")
                    }

                    add(
                        Line(0.0, 0.0, 0.0, 0.0).apply {
                            endYProperty().bind(this@region.heightProperty())

                            style {
                                stroke = Paint.valueOf("#ffb100")
                            }
                        }
                    )
                    add(PlaceMarkerLayer())
                }
            }
        }

        bottom {
            region {

                prefHeight = 50.0

                style {
                    borderWidth += box(1.px)
                    borderColor += box(null, null, Paint.valueOf("#a7b6cf"), null)
                    backgroundColor += Paint.valueOf("#ced6e3")
                }
                stackpane {
                    add(
                        timecodeImageView.apply {
                            translateYProperty()
                                .bind(this@region.heightProperty() / 2 - timecodeImageView.image.height / 2)
                        }
                    )
                    add(
                        Line(0.0, 0.0, 0.0, 0.0).apply {
                            endYProperty().bind(this@region.heightProperty())

                            style {
                                stroke = Paint.valueOf("#ffb100")
                            }
                        }
                    )
                }
            }
        }
    }
}
