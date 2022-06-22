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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.NodeOrientation
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import tornadofx.*

class MarkerPlacementWaveform : StackPane() {

    val markers = observableListOf<ChunkMarkerModel>()
    var onPositionChangedProperty: (Int, Double) -> Unit = { _, _ -> }
    var onSeekPreviousProperty: () -> Unit = {}
    var onSeekNextProperty: () -> Unit = {}
    var onLocationRequestProperty: () -> Int = { 0 }

    val markerStateProperty = SimpleObjectProperty<VerseMarkerModel>()

    val imageWidthProperty = SimpleDoubleProperty()

    var onSeekNext: () -> Unit = {}
    var onSeekPrevious: () -> Unit = {}
    var onPlaceMarker: () -> Unit = {}


    val positionProperty = SimpleDoubleProperty(0.0)
    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}
    var onRewind: ((ScrollSpeed) -> Unit) = {}
    var onFastForward: ((ScrollSpeed) -> Unit) = {}
    var onToggleMedia: () -> Unit = {}
    var onResumeMedia: () -> Unit = {}

    private val waveformFrame: WaveformFrame

    fun freeImages() {
        waveformFrame.freeImages()
    }

    fun addWaveformImage(image: Image) {
        waveformFrame.addImage(image)
    }

    private lateinit var top: MarkerTrackControl

    fun refreshMarkers() {
        top.refreshMarkers()
    }

    init {
        minHeight = 120.0

        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        add(MarkerViewBackground())

        val topTrack = MarkerTrackControl().apply {
            top = this
            markers.bind(this@MarkerPlacementWaveform.markers, { it })
            setOnPositionChanged { id, position ->
                this@MarkerPlacementWaveform.onPositionChangedProperty.invoke(id, position)
            }
            setOnLocationRequest {
                this@MarkerPlacementWaveform.onLocationRequestProperty.invoke()
            }
        }
        waveformFrame = WaveformFrame(topTrack).apply {
            framePositionProperty.bind(positionProperty)
            onWaveformClicked { onWaveformClicked() }
            onWaveformDragReleased {
                onWaveformDragReleased(it)
            }
            onRewind(onRewind)
            onFastForward(onFastForward)
            onToggleMedia(onToggleMedia)
            onResumeMedia(onResumeMedia)
            onSeekPrevious(this@MarkerPlacementWaveform.onSeekPrevious)
            onSeekNext(this@MarkerPlacementWaveform.onSeekNext)

            focusedProperty().onChange {
                togglePseudoClass("active", it)
            }
        }
        add(waveformFrame)
        add(WaveformOverlay().apply { playbackPositionProperty.bind(positionProperty) })
        add(PlaceMarkerLayer().apply { onPlaceMarkerAction { onPlaceMarker() } })
    }
}
