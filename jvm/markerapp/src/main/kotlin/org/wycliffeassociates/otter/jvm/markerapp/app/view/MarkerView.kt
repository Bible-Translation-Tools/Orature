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

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

const val WINDOW_OFFSET = 50.0

class MarkerView : PluginEntrypoint() {

    val viewModel: VerseMarkerViewModel by inject()

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val source = SourceTextFragment()
    val playbackControls = PlaybackControlsFragment()

    init {
        viewModel.onDock()
        runLater {
            val css = this@MarkerView.javaClass.getResource("/css/verse-marker-app.css")
                .toExternalForm()
                .replace(" ", "%20")
            tryImportStylesheet(css)

            FX.stylesheets.addAll(
                javaClass.getResource("/css/control.css").toExternalForm(),
                css
            )
        }
        viewModel.initializeAudioController(minimap.slider)
    }

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - WINDOW_OFFSET
        prefWidth = Screen.getPrimary().visualBounds.width - WINDOW_OFFSET

        add(titleFragment)
        add(minimap)
        add(waveformContainer)
        add(source)
        add(playbackControls)
    }
}
