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

import javafx.beans.value.ChangeListener
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class MinimapFragment : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()

    lateinit var waveformMinimapListener: ChangeListener<Image>

    val slider = AudioSlider().apply {
        waveformMinimapListener = ChangeListener { _, _, it ->
            waveformImageProperty.set(it)
        }
        viewModel.waveformMinimapImage.addListener(waveformMinimapListener)

        player.set(viewModel.audioPlayer)
        secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
    }

    override val root = hbox {
        alignment = Pos.CENTER_LEFT
        styleClass.add("vm-minimap-container")
        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            button {
                graphic = FontIcon("gmi-bookmark")
                styleClass.add("vm-marker-count__icon")
            }
            add(
                label().apply {
                    textProperty().bind(viewModel.markerRatioProperty)
                }
            )
        }
        add(
            slider.apply {
                hgrow = Priority.ALWAYS
            }
        )
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.waveformMinimapImage.removeListener(waveformMinimapListener)
        slider.clearListeners()
    }
}
