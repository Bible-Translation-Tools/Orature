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
package org.wycliffeassociates.otter.common.domain.translation

import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants.SOURCE_AUDIO_DIR
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ChunkAudioUseCase(val directoryProvider: IDirectoryProvider, val projectFilesAccessor: ProjectFilesAccessor) {
    fun createChunkedSourceAudio(source: File, cues: List<AudioCue>) {
        val temp = File(directoryProvider.tempDirectory, source.name).apply { createNewFile() }
        val tempCue = File(temp.parent, "${temp.nameWithoutExtension}.cue").apply { createNewFile() }

        try {
            source.copyTo(temp, true)
            val audio = OratureAudioFile(temp)
            audio.clearChunkMarkers()
            cues.sortedBy { it.location }.forEachIndexed { index, cue ->
                audio.addMarker(ChunkMarker(index + 1, cue.location))
            }
            audio.update()
            val path = projectFilesAccessor.projectDir
            ResourceContainer.load(path).use {
                it.addFileToContainer(temp, "${SOURCE_AUDIO_DIR}/${temp.name}")
                if (tempCue.exists()) {
                    it.addFileToContainer(tempCue, "${SOURCE_AUDIO_DIR}/${tempCue.name}")
                }
                it.write()
            }
        } finally {
            temp.delete()
            if (tempCue.exists()) {
                tempCue.delete()
            }
        }
    }

    fun copySourceAudioToProject(source: File) {
        val path = projectFilesAccessor.projectDir
        ResourceContainer.load(path).use { rc ->
            val targetPath = "${SOURCE_AUDIO_DIR}/${source.name}"
            if (!rc.accessor.fileExists(targetPath)) {
                rc.addFileToContainer(source, targetPath)
            }
        }
    }
}
