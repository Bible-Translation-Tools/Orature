/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import javafx.animation.AnimationTimer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()

    private val markerTrack: MarkerTrackControl = MarkerTrackControl().apply {
        prefWidth = viewModel.imageWidth
        viewModel.markerStateProperty.onChangeAndDoNow { markers ->
            markers?.let { markers ->
                markers.markerCountProperty?.onChangeAndDoNow {
                    this.markers.setAll(viewModel.markers.markers)
                    highlightState.setAll(viewModel.markers.highlightState)
                    refreshMarkers()
                }
            }
        }
    }

    override val root = MarkerPlacementWaveform(markerTrack).apply {
        positionProperty.bind(viewModel.positionProperty)

        onSeekNext = viewModel::seekNext
        onSeekPrevious = viewModel::seekPrevious

        onPlaceMarker = viewModel::placeMarker
        onWaveformClicked = { viewModel.pause() }
        onWaveformDragReleased = { deltaPos ->
            val deltaFrames = pixelsToFrames(deltaPos)
            val curFrames = viewModel.getLocationInFrames()
            val duration = viewModel.getDurationInFrames()
            val final = Utils.clamp(0, curFrames - deltaFrames, duration)
            viewModel.seek(final)
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.compositeDisposable.addAll(
            viewModel.waveform.observeOnFx().subscribe {
                root.addWaveformImage(it)
            },
            viewModel.waveformAsyncBuilder.observeOnFx().subscribe()
        )
    }

    override fun onUndock() {
        super.onUndock()
        root.freeImages()
    }

    init {
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                viewModel.calculatePosition()
            }
        }.start()
    }
}
