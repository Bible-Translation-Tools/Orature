/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import java.io.File
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.CreatePlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject

class AddPluginViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(AddPluginViewModel::class.java)

    @Inject lateinit var pluginRepository: IAudioPluginRepository

    private val settingsViewModel: SettingsViewModel by inject()

    val nameProperty = SimpleStringProperty()
    private val name by nameProperty
    val pathProperty = SimpleStringProperty()
    private val path by pathProperty
    val canEditProperty = SimpleBooleanProperty()
    private val canEdit by canEditProperty
    val canRecordProperty = SimpleBooleanProperty()
    private val canRecord by canRecordProperty

    val validProperty = SimpleBooleanProperty(false)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        validProperty.bind(
            nameProperty.isNotEmpty
                .and(pathProperty.isNotEmpty)
                .and(canRecordProperty.or(canEditProperty))
        )
    }

    fun save() {
        // Create the audio plugin
        if (!validProperty.value) return

        val pluginData = AudioPluginData(
            0,
            name,
            "1.0.0",
            canEdit,
            canRecord,
            false,
            path,
            listOf(),
            null
        )
        CreatePlugin(pluginRepository)
            .create(pluginData)
            .doOnSuccess {
                pluginData.id = it

                settingsViewModel.selectRecorder(pluginData)
                settingsViewModel.selectEditor(pluginData)
                settingsViewModel.refreshPlugins()
            }
            .doOnError { e ->
                logger.error("Error creating a plugin:")
                logger.error("Plugin name: $name, path: $path, record: $canRecord, edit: $canEdit", e)
            }
            .onErrorComplete()
            .subscribe()
    }

    /**
     * Allows for completing the path to the plugin binary, if needed based on platform.
     * Currently only adds the remaining path after the .app directory on MacOS.
     */
    fun completePluginPath(path: String): String {
        if (File(path).isDirectory && path.matches(Regex(".*.app"))) {
            val macPath = File(path, "Contents/MacOS/")
            if (macPath.exists()) {
                // This directory may have multiple files, but we can't necessarily know the binary name
                // thus, we'll try the first, and if it's wrong, the user can edit the text after.
                return macPath.listFiles().first().absolutePath
            }
        }
        return path
    }
}
