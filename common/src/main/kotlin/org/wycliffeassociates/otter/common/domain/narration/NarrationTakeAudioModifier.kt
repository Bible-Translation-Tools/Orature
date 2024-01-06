package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile

class NarrationTakeAudioModifier(val take: Take) {
    var oaf: OratureAudioFile

    init {
        oaf = if (!take.file.exists()) {
            OratureAudioFile(
                take.file,
                DEFAULT_CHANNELS,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_BITS_PER_SAMPLE
            )
        } else {
            OratureAudioFile(take.file)
        }
    }

    private val audioBounceTaskRunner = NarrationAudioBouncerTaskRunner

    fun modifyAudioData(reader: AudioFileReader, markers: List<AudioMarker>) {
        // May need to examine the history. The reader is a shared resource, and we may need to get a snapshot of it
        // when bouncing the audio.
        oaf = OratureAudioFile(take.file)
        audioBounceTaskRunner.bounce(oaf.file, reader, markers)

    }

    fun modifyMetadata(markers: List<AudioMarker>) {
        oaf = OratureAudioFile(take.file)
        audioBounceTaskRunner.updateMarkers(oaf, markers)
    }
}