package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.javafx.util.Utils
import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    var dragStart: Point2D? = null
    var posAtTimeOfDrag = 0

    val viewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    val markerTrack: MarkerTrackControl
    // val timecodeHolder: TimecodeHolder

    init {
        markerTrack = MarkerTrackControl(viewModel.markers.markers, viewModel.markers.highlightState).apply {
            prefWidth = viewModel.imageWidth
            viewModel.markers.markerCountProperty.onChange {
                refreshMarkers()
            }
        }
        // timecodeHolder = TimecodeHolder(viewModel, 50.0)
        mainWaveform = MainWaveform(viewModel)

        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (mainWaveform.image != null) {
                    viewModel.calculatePosition()
                }
            }
        }.start()
    }

    override val root =
        stackpane {
            slider()
            setOnMousePressed { me ->
                println("clicked\n\n\n")
                viewModel.audioPlayer.pause()
                posAtTimeOfDrag = viewModel.audioPlayer.getAbsoluteLocationInFrames()
                val trackWidth = this@stackpane.width
                if (trackWidth > 0) {
                    dragStart = localToParent(me.x, me.y)
                    me.consume()
                }
            }

            setOnMouseDragged { me ->
                val trackWidth = this@stackpane.width
                if (trackWidth > 0.0) {
                    //if (trackWidth > this.width) {
                        val cur: Point2D = localToParent(me.x, me.y)
                        if (dragStart == null) {
                            // we're getting dragged without getting a mouse press
                            dragStart = localToParent(me.x, me.y)

                        }
                        val dragPos = cur.x - dragStart!!.x
                        val frames = viewModel.audioPlayer.getAbsoluteLocationInFrames()
                        println("current location is ${frames}")
                        val delta = pixelsToFrames(dragPos)
                        println("delta is $delta, dragpos is ${dragPos}, dragStart is ${dragStart!!.x}")
                        val final = Utils.clamp(0,frames - delta, viewModel.audioPlayer.getAbsoluteDurationInFrames())
                        println("seeking to $final")
                        viewModel.audioPlayer.seek(final)
                        dragStart = localToParent(me.x, me.y)
                    //}
                    me.consume()
                }
            }

            setOnMouseReleased {
                dragStart = null
                println("released")
            }

            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            styleClass.add("vm-waveform-container")

            add(MarkerViewBackground())
            add(
                WaveformFrame(
                    markerTrack,
                    mainWaveform,
                  //  timecodeHolder,
                    viewModel
                )
            )
            add(WaveformOverlay(viewModel))
            add(PlaceMarkerLayer(viewModel))
        }
}
