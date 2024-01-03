package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile

class NarrationTakeAudioModifier(val take: Take) {

    init {
        if (!take.file.exists()) {
            OratureAudioFile(
                take.file,
                DEFAULT_CHANNELS,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_BITS_PER_SAMPLE
            )
        }
    }

    private val audioBounceTaskRunner = NarrationAudioBouncerTaskRunner

    fun modifyAudioData(reader: AudioFileReader, markers: List<AudioMarker>) {
        val oaf = OratureAudioFile(take.file)
        audioBounceTaskRunner.bounce(oaf.file, reader, markers)
    }

    fun modifyMetaData(markers: List<AudioMarker>) {
        val oaf = OratureAudioFile(take.file)
        clearNarrationMarkers(oaf)
        addNarrationMarkers(oaf, markers)
        oaf.update()
    }

    private fun clearNarrationMarkers(audioFile: OratureAudioFile) {
        audioFile.clearMarkersOfType<VerseMarker>()
        audioFile.clearMarkersOfType<ChapterMarker>()
        audioFile.clearMarkersOfType<BookMarker>()
    }

    private fun addNarrationMarkers(audioFile: OratureAudioFile, markers: List<AudioMarker>) {
        markers.forEach { marker ->
            audioFile.addMarker(audioFile.getMarkerTypeFromClass(marker::class), marker)
        }
    }
}