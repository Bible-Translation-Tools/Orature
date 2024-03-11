package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.domain.narration.statemachine.VerseItemState

class NarratableItemData(
    val chunk: Chunk,
    var marker: AudioMarker?,
    var verseState: VerseItemState = VerseItemState.RECORD_DISABLED,
    var previousChunksRecorded: Boolean = false,
    var playEnabled: Boolean = false,
    var editVerseEnabled: Boolean = false,
    var recordAgainEnabled: Boolean = false,
) {
    override fun toString(): String {
        return "${chunk.sort}, $verseState, $previousChunksRecorded"
    }
}



