package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File

object VttTimingFile {
    fun create(
        audio: OratureAudioFile,
        bookSlug: String,
        chapterNumber: Int,
        vttFile: File = File(audio.file.parent, "${audio.file.nameWithoutExtension}.vtt")
    ) {
        if (vttFile.exists()) {
            vttFile.delete()
            vttFile.createNewFile()
        }

        val markers = audio.getMarkers()

        OratureVttMetadata(vttFile)
            .write(
                markers,
                bookSlug,
                chapterNumber,
                audio.totalFrames
            )
    }

    fun importMarkersFromVttFile(
        vttFile: File,
        audioToImportMarkersTo: OratureAudioFile
    ) {
        val markers = OratureVttMetadata(vttFile).parseVTTFile()

        audioToImportMarkersTo.clearMarkersOfType<BookMarker>()
        audioToImportMarkersTo.clearMarkersOfType<ChapterMarker>()
        audioToImportMarkersTo.clearMarkersOfType<VerseMarker>()
        audioToImportMarkersTo.clearMarkersOfType<ChunkMarker>()

        audioToImportMarkersTo.importCues(markers.getCues())
    }
}