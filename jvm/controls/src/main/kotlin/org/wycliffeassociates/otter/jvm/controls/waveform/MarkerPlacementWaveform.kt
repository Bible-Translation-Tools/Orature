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
import javafx.scene.Node
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.MarkerPlacementWaveformSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.observableListOf

class MarkerPlacementWaveform : ScrollingWaveform() {

    val markers = observableListOf<ChunkMarkerModel>()
    var onPositionChangedProperty: (Int, Double) -> Unit = { _, _ -> }
    var onSeekPreviousProperty: () -> Unit = {}
    var onSeekNextProperty: () -> Unit = {}
    var onLocationRequestProperty: () -> Int = { 0 }

    val imageWidthProperty = SimpleDoubleProperty()

    var onSeekNext: () -> Unit = {}
    var onSeekPrevious: () -> Unit = {}
    var onPlaceMarker: () -> Unit = {}

    fun refreshMarkers() {
        (skin as MarkerPlacementWaveformSkin).refreshMarkers()
    }

    init {
        skinProperty().onChangeAndDoNow { it?.let { refreshMarkers() } }
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerPlacementWaveformSkin(this)
    }
}
