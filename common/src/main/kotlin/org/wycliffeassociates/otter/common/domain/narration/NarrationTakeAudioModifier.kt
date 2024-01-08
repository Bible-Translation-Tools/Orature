package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Take

class NarrationTakeAudioModifier(val take: Take) {

    private val audioBounceTaskRunner = NarrationTakeModifierTaskRunner
    lateinit var isBusy: Observable<Boolean>

    init {
        isBusy = Observable.combineLatest(
            audioBounceTaskRunner.audioBouncerBusy.startWith(false),
            audioBounceTaskRunner.markerUpdateBusy.startWith(false)
        ) { audioBounceBusy, markerUpdateBusy ->
            audioBounceBusy || markerUpdateBusy
        }
            .distinctUntilChanged()
    }

    fun modifyAudioData(reader: AudioFileReader, markers: List<AudioMarker>) {
        // May need to examine the history. The reader is a shared resource, and we may need to get a snapshot of it
        // when bouncing the audio.
        audioBounceTaskRunner.bounce(take.file, reader, markers)
    }

    fun modifyMetadata(markers: List<AudioMarker>) {
        audioBounceTaskRunner.updateMarkers(take.file, markers)
    }
}