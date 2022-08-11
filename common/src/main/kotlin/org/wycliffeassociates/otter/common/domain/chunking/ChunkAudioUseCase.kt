package org.wycliffeassociates.otter.common.domain.chunking

import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ChunkAudioUseCase(val directoryProvider: IDirectoryProvider, val workbook: Workbook) {
    fun createChunkedSourceAudio(source: File, cues: List<AudioCue>) {
        val temp = File(directoryProvider.tempDirectory, source.name).apply { createNewFile() }
        val tempCue = File(temp.parent, "${temp.nameWithoutExtension}.cue").apply { createNewFile() }

        val accessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            workbook.target.resourceMetadata,
            workbook.target
        )
        try {
            source.copyTo(temp, true)
            val audio = AudioFile(temp)
            audio.metadata.clearMarkers()
            audio.update()
            for (cue in cues) {
                audio.metadata.addCue(cue.location, cue.label)
            }
            audio.update()
            val path = accessor.projectDir
            ResourceContainer.load(path).use {

                it.addFileToContainer(temp, ".apps/orature/source/audio/${temp.name}")
                if (tempCue.exists()) {
                    it.addFileToContainer(tempCue, ".apps/orature/source/audio/${tempCue.name}")
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
}
