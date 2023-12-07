package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile

class NarrationTakeAudioModifier(take: Take) {

    lateinit var audioFile : OratureAudioFile

    init {
        audioFile = OratureAudioFile(take.file)
    }

    fun modifyAudioData() {
        // TODO: finish
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