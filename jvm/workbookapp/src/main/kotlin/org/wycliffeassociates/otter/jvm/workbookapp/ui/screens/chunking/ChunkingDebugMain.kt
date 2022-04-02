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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import java.io.File
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.device.audio.DEFAULT_AUDIO_FORMAT
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

fun main(args: Array<String>) {
    launch<ChunkingDebugApp>(args)
}

class ChunkingDebugApp : App(ChunkingDebugView::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()

    init {
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/control.css"])
        ConfigureAudioSystem(
            dependencyGraph.injectConnectionFactory(),
            AudioDeviceProvider(DEFAULT_AUDIO_FORMAT),
            dependencyGraph.injectAppPreferencesRepository()
        ).configure()

        ConfigureLogger(dependencyGraph.injectDirectoryProvider().logsDirectory).configure()
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
    }
}

class ChunkingDebugView : View(){
    override val root = StackPane().apply { addClass("light-theme") }

    init {
        val wkbk: WorkbookDataStore = tornadofx.find()
        wkbk.sourceAudioProperty.set(SourceAudio(File("/Users/joe/Documents/test12345.mp3"), 0, 1))
        add<ChunkingWizard>()
    }
}
