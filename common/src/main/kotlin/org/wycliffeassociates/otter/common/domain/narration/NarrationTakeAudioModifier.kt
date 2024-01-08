package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile

class NarrationTakeAudioModifier(val take: Take) {

    private val audioBounceTaskRunner = NarrationAudioBouncerTaskRunner

    fun modifyAudioData(reader: AudioFileReader, markers: List<AudioMarker>) {
        // May need to examine the history. The reader is a shared resource, and we may need to get a snapshot of it
        // when bouncing the audio.
        audioBounceTaskRunner.bounce(take.file, reader, markers)

    }

    fun modifyMetadata(markers: List<AudioMarker>) {
        audioBounceTaskRunner.updateMarkers(take.file, markers)
    }
}