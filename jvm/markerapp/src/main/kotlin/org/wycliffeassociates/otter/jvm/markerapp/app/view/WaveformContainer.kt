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
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()
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

        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                viewModel.calculatePosition()
            }
        }.start()
    }

    override val root =
        stackpane {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            styleClass.add("vm-waveform-container")

            add(MarkerViewBackground())
            add(
                WaveformFrame(
                    markerTrack,
                  //  timecodeHolder,
                    viewModel
                )
            )
            add(WaveformOverlay(viewModel))
            add(PlaceMarkerLayer(viewModel))
        }
}
