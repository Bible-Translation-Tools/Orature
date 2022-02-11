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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

class RecorderView : PluginEntrypoint() {

    private var viewInflated = false

    private val waveform = RecordingVisualizerFragment()

    private val spacer = region().apply {
        prefHeight = 2.0
    }

    private val recorderViewModel: RecorderViewModel by inject()

    override val root = vbox {
        prefHeight = Screen.getPrimary().visualBounds.height - 50.0
        prefWidth = Screen.getPrimary().visualBounds.width - 50.0

        add<InfoFragment>()
        add(spacer)
        add(waveform)
        add<SourceAudioFragment>()
        add<ControlFragment>()
    }

    init {
        tryImportStylesheet(resources.get("/css/recorder.css"))

        // notifies viewmodel that views have been inflated and the canvas now has a width
        waveform.root.widthProperty().onChange { width ->
            if (!viewInflated && width.toInt() > 0) {
                recorderViewModel.onViewReady(width.toInt())
                viewInflated = true
            }
        }
    }
}
