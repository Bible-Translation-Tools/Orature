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
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants.SOURCE_AUDIO_DIR
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ChunkAudioUseCase(val directoryProvider: IDirectoryProvider, val projectFilesAccessor: ProjectFilesAccessor) {

    private val logger = LoggerFactory.getLogger(ChunkAudioUseCase::class.java)

    /**
     * Replaces the chunk markers of the project's copy of the chapter source audio, in place.
     *
     * For wav, this only rewrites the metadata that follows the audio data and the header.
     * For mp3, this only rewrites the cue file that sits next to the mp3.
     *
     * The file must not be held open (memory mapped) by a player while this runs; on Windows the wav update
     * truncates the file, which fails while a mapping is open. Use [createWorkingCopy] for playback instead.
     *
     * @param projectSourceAudio the chapter source audio inside the project's source audio directory
     * @param cues the chunk boundaries, in any order
     */
    fun updateChunkMarkers(projectSourceAudio: File, cues: List<AudioCue>) {
        val audio = OratureAudioFile(projectSourceAudio)
        audio.clearChunkMarkers()
        cues.sortedBy { it.location }.forEachIndexed { index, cue ->
            audio.addMarker(ChunkMarker(index + 1, cue.location))
        }
        audio.update()
    }

    /**
     * Copies the given audio file (and its cue file, for mp3) to a uniquely named file in the app's temp directory,
     * so that it can be played while the original is being written to.
     *
     * Delete the copy with [deleteWorkingCopy] once nothing is reading from it anymore.
     */
    fun createWorkingCopy(source: File): File {
        val tempDir = directoryProvider.tempDirectory.apply { mkdirs() }
        val copy = File.createTempFile("chunking-${source.nameWithoutExtension}-", ".${source.extension}", tempDir)
        source.copyTo(copy, overwrite = true)

        // mp3 markers are read from a cue file with the same name next to the mp3
        val sourceCue = cueFileFor(source)
        if (sourceCue.exists()) {
            sourceCue.copyTo(cueFileFor(copy), overwrite = true)
        }
        return copy
    }

    fun deleteWorkingCopy(copy: File) {
        listOf(copy, cueFileFor(copy)).forEach { file ->
            if (file.exists() && !file.delete()) {
                // anything left over is removed when the app's temp directory is cleaned on exit
                logger.warn("Could not delete chunking working copy: ${file.path}")
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

    private fun cueFileFor(audio: File) = File(audio.parentFile, "${audio.nameWithoutExtension}.cue")
}
