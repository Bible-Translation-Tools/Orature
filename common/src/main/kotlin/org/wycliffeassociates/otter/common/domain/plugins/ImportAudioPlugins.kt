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
package org.wycliffeassociates.otter.common.domain.plugins

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ImportAudioPlugins(
    private val pluginRegistrar: IAudioPluginRegistrar,
    private val directoryProvider: IDirectoryProvider
) {
    fun importAll(): Completable {
        // Imports all the plugins from the workbookapp's plugin directory
        return pluginRegistrar.importAll(directoryProvider.audioPluginDirectory)
    }

    fun importExternal(externalPluginFile: File): Completable {
        // Import the external file and copy it into the workbookapp's plugin directory
        val newFile = directoryProvider.audioPluginDirectory.resolve(externalPluginFile.name)
        externalPluginFile.copyTo(newFile, true)
        return pluginRegistrar.import(newFile)
    }
}