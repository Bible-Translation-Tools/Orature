package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MainWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.TimecodeHolder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val verseMarkerViewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    val playedOverlay = Rectangle()
    val positionProperty = SimpleDoubleProperty(0.0)
    val markerTrack: Region
    val timecodeHolder: TimecodeHolder

    init {

        val width = Screen.getMainScreen().platformWidth
        val height = Screen.getMainScreen().platformHeight

        val imageWidth =
            (44100 * 5 / width.toDouble()) * (verseMarkerViewModel.audioPlayer.getAbsoluteDurationMs() / 1000.0)

        markerTrack = MarkerTrack(verseMarkerViewModel, imageWidth, 50.0)
        timecodeHolder = TimecodeHolder(imageWidth, 50.0, verseMarkerViewModel.audioPlayer.getAbsoluteDurationMs())

        mainWaveform = MainWaveform(verseMarkerViewModel.audioPlayer.getAudioReader()!!, imageWidth.toInt(), height)
    }

    override val root = borderpane {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        val ht = Screen.getMainScreen().platformHeight
        val wd = Screen.getMainScreen().platformWidth

        val at = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (mainWaveform.image != null) {
                    val player = verseMarkerViewModel.audioPlayer
                    val padding = Screen.getMainScreen().platformWidth / 2
                    val width = mainWaveform.image.width
                    val pos =
                        (player.getAbsoluteLocationInFrames() / player.getAbsoluteDurationInFrames().toDouble()) * width
                    positionProperty.set(pos)
                    mainWaveform.scrollTo(pos - padding)
                    timecodeHolder.scrollTo(pos - padding)
                    val scaleFactor = widthProperty().get() / Screen.getMainScreen().platformWidth.toDouble()
                    val trackOffset = (widthProperty().get() * 1.355) - 1231
                    markerTrack.scaleXProperty().set(scaleFactor)
                    markerTrack.translateXProperty().set(trackOffset - pos * scaleFactor)
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
                mainWaveform.fitToSize(this@region)

                stackpane {
                    alignment = Pos.CENTER

                    fitToParentWidth()
                    fitToParentHeight()

                    add(mainWaveform)
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
                    timecodeHolder.fitToWidth(this@region)
                    add(timecodeHolder)
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
