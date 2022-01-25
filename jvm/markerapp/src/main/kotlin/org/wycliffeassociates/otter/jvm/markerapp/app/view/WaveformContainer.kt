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
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

interface ImageDisposer {
    fun freeImages()
}

class WaveformContainer : Fragment(), ImageDisposer {

    val viewModel: VerseMarkerViewModel by inject()

    val markerTrack: MarkerTrackControl = MarkerTrackControl(
        viewModel.markers.markers,
        viewModel.markers.highlightState
    ).apply {
        prefWidth = viewModel.imageWidth
        viewModel.markers.markerCountProperty.onChange {
            refreshMarkers()
        }
    }

    lateinit var waveformFrame: WaveformFrame
    // val timecodeHolder: TimecodeHolder

    override val root = ScrollingWaveform(
        viewModel.positionProperty,
        viewModel::placeMarker,
        viewModel::pause,
        { deltaPos ->
            val deltaFrames = pixelsToFrames(deltaPos)
            val curFrames = viewModel.getLocationInFrames()
            val duration = viewModel.getDurationInFrames()
            val final = Utils.clamp(0, curFrames - deltaFrames, duration)
            viewModel.seek(final)
        },
        markerTrack
    )

    init {
        viewModel.registerImageDisposer(this)

        // timecodeHolder = TimecodeHolder(viewModel, 50.0)

        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                viewModel.calculatePosition()
            }
        }.start()

        val disposable = viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveformFrame.addImage(it)
            }

        // ready to receive images, start building waveform
        val disposableBuilder = viewModel.waveformAsyncBuilder
            .observeOnFx()
            .subscribe()

        viewModel.compositeDisposable.addAll(disposable, disposableBuilder)

        waveformFrame.addHighlights(viewModel.markers.highlightState)
    }

    override fun freeImages() {
        waveformFrame.freeImages()
    }
}
