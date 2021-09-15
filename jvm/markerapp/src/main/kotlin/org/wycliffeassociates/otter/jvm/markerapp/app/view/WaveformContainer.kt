/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.javafx.util.Utils
import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    val markerTrack: MarkerTrackControl
    // val timecodeHolder: TimecodeHolder

    var dragStart: Point2D? = null

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
//                if (mainWaveform.image != null) {
//                    viewModel.calculatePosition()
//                }
                viewModel.calculatePosition()
            }
        }.start()
    }

    override val root =
        stackpane {
            setOnMousePressed { me ->
                viewModel.pause()
                val trackWidth = this@stackpane.width
                if (trackWidth > 0) {
                    dragStart = localToParent(me.x, me.y)
                    me.consume()
                }
            }

            setOnMouseDragged { me ->
                val trackWidth = this@stackpane.width
                if (trackWidth > 0.0) {
                    val cur: Point2D = localToParent(me.x, me.y)
                    if (dragStart == null) {
                        // we're getting dragged without getting a mouse press
                        dragStart = localToParent(me.x, me.y)
                    }
                    val deltaPos = cur.x - dragStart!!.x
                    val deltaFrames = pixelsToFrames(deltaPos)

                    val curFrames = viewModel.getLocationInFrames()
                    val duration = viewModel.getDurationInFrames()
                    val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                    viewModel.seek(final)
                    dragStart = localToParent(me.x, me.y)
                    me.consume()
                }
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
