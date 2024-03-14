package org.wycliffeassociates.otter.common.domain.narration.statemachine

class NarratableItem(
    var verseState: VerseItemState = VerseItemState.RECORD_DISABLED,
    var isPlayOptionEnabled: Boolean = false,
    var isEditVerseOptionEnabled: Boolean = false,
    var isRecordAgainOptionEnabled: Boolean = false,
) {

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
}