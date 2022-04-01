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
package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import javafx.geometry.NodeOrientation
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerViewBackground
import org.wycliffeassociates.otter.jvm.controls.waveform.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformFrame
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformOverlay
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerPlacementWaveformSkin(val control: MarkerPlacementWaveform) : ScrollingWaveformSkin(control) {

    private fun addHighlights(highlights: List<MarkerHighlightState>) {
        waveformFrame.clearHighlights()

        highlights.forEach {
            waveformFrame.addHighlight(
                Rectangle().apply {
                    managedProperty().set(false)
                    widthProperty().bind(it.width)
                    translateXProperty().bind(it.translate)
                    visibleProperty().bind(it.visibility)
                    it.styleClass.onChangeAndDoNow {
                        styleClass.setAll(it)
                    }
                }
            )
        }
    }

    override fun initialize() {
        val root = StackPane().apply {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

            add(MarkerViewBackground())
            waveformFrame = WaveformFrame(
                (skinnable as MarkerPlacementWaveform).topTrack
            ).apply {
                framePositionProperty.bind(skinnable.positionProperty)
                onWaveformClicked { skinnable.onWaveformClicked() }
                onWaveformDragReleased {
                    skinnable.onWaveformDragReleased(it)
                }
                onRewind(skinnable.onRewind)
                onFastForward(skinnable.onFastForward)
                onToggleMedia(skinnable.onToggleMedia)
                onResumeMedia(skinnable.onResumeMedia)
                onSeekPrevious((skinnable as MarkerPlacementWaveform).onSeekPrevious)
                onSeekNext((skinnable as MarkerPlacementWaveform).onSeekNext)

                focusedProperty().onChange {
                    skinnable.togglePseudoClass("active", it)
                }
            }
            add(waveformFrame)
            add(WaveformOverlay().apply { playbackPositionProperty.bind(skinnable.positionProperty) })
            add(PlaceMarkerLayer().apply { onPlaceMarkerAction { control.onPlaceMarker() } })

            (skinnable as MarkerPlacementWaveform).markerStateProperty.onChangeAndDoNow { markers ->
                markers?.let { markers ->
                    addHighlights(markers.highlightState)
                }
            }
        }
        children.add(root)
    }
}
