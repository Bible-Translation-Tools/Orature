package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.domain.narration.statemachine.VerseItemState

class NarratableItemData(
    val chunk: Chunk,
    var marker: AudioMarker?,
    var verseState: VerseItemState = VerseItemState.RECORD_DISABLED,
    var previousChunksRecorded: Boolean = false,
    var isPlayOptionEnabled: Boolean = false,
    var isEditVerseOptionEnabled: Boolean = false,
    var isRecordAgainOptionEnabled: Boolean = false,
) {
    override fun toString(): String {
        return "${chunk.sort}, $verseState, $previousChunksRecorded"
    }

    private val statesWithoutRecording = listOf(
        VerseItemState.BEGIN_RECORDING,
        VerseItemState.RECORD,
        VerseItemState.RECORD_DISABLED
    )

    private val activeStates = listOf(
        VerseItemState.RECORD_ACTIVE,
        VerseItemState.RECORD_AGAIN_ACTIVE,
        VerseItemState.RECORDING_PAUSED,
        VerseItemState.RECORD_AGAIN_PAUSED
    )

    val hasRecording: Boolean
        get() = verseState !in statesWithoutRecording

    val isActiveRecording: Boolean
        get() = verseState in activeStates

    // TODO note: I want to add a method like updateOptionValues(NarrationStateType), but I am not sure if that is a
    //  proper thing to do for a Model class.
}



