package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Take

fun modifyAudioData(take: Take?, reader: AudioFileReader, markers: List<AudioMarker>) {
    if (take == null) return
    // May need to examine the history. The reader is a shared resource, and we may need to get a snapshot of it
    // when bouncing the audio.
    NarrationTakeModifierTaskRunner.bounce(take.file, reader, markers)
}

fun modifyMetadata(take: Take?, markers: List<AudioMarker>) {
    if (take == null) return

    NarrationTakeModifierTaskRunner.updateMarkers(take.file, markers)
}
