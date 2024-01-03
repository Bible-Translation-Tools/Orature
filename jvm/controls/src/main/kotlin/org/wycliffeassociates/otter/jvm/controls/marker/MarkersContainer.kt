/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.marker

import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import tornadofx.*

class MarkersContainer: MarkerTrackControl() {
    override fun createMarker(): MarkerControl {
        return MarkerNode().apply {
            prefHeightProperty().bind(this@MarkersContainer.heightProperty())
        }
    }

    /**
     * Modifies the creation of marker rectangles to cover the waveform area
     * starting from the top track with n pixels vertical offset (translateY).
     */
    override fun preallocateMarkers() {
        for (i in 0 until MARKER_COUNT) {
            val mk = ChunkMarkerModel(0, i.toString(), false)
            val marker = createMarker(i, mk)
            val rect = createHighlight(i, mk)
            val headerOffset = 80.0
            rect.heightProperty().bind(heightProperty().minus(headerOffset))
            rect.translateXProperty().bind(marker.translateXProperty())
            rect.translateY = headerOffset
            rect.visibleProperty().bind(marker.visibleProperty())

            _markers.add(marker)
            highlights.add(rect)
        }
    }
}
