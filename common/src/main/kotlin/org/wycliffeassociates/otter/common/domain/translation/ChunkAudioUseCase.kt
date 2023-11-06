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
