package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile

class NarrationTakeAudioModifier(val take: Take, createNewAudioFile: Boolean = false) {

    private var audioFile: OratureAudioFile = if (createNewAudioFile) {
        OratureAudioFile(
            take.file,
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE
        )
    } else {
        OratureAudioFile(take.file)
    }

    fun modifyAudioData(reader: AudioFileReader, markers: List<AudioMarker>) {
        bounceAudio(
            audioFile.file,
            reader,
            markers
        )
    }

    fun modifyMetaData(markers: List<AudioMarker>) {
        clearNarrationMarkers()
        addNarrationMarkers(markers)
        audioFile.update()
    }

    private fun clearNarrationMarkers() {
        audioFile.clearMarkersOfType<VerseMarker>()
        audioFile.clearMarkersOfType<ChapterMarker>()
        audioFile.clearMarkersOfType<BookMarker>()
    }

    private fun addNarrationMarkers(markers: List<AudioMarker>) {
        markers.forEach { marker ->
            audioFile.addMarker(marker)
        }
    }
}