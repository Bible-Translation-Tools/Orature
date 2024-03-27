package org.wycliffeassociates.otter.common.domain.narration.teleprompter

class NarratableItem(
    var verseState: TeleprompterItemState = TeleprompterItemState.RECORD_DISABLED,
    var isPlayOptionEnabled: Boolean = false,
    var isEditVerseOptionEnabled: Boolean = false,
    var isRecordAgainOptionEnabled: Boolean = false,
) {

    private val statesWithoutRecording = listOf(
        TeleprompterItemState.BEGIN_RECORDING,
        TeleprompterItemState.RECORD,
        TeleprompterItemState.RECORD_DISABLED
    )

    private val activeStates = listOf(
        TeleprompterItemState.RECORD_ACTIVE,
        TeleprompterItemState.RECORD_AGAIN_ACTIVE,
        TeleprompterItemState.RECORDING_PAUSED,
        TeleprompterItemState.RECORD_AGAIN_PAUSED
    )

    val hasRecording: Boolean
        get() = verseState !in statesWithoutRecording

    val isActiveRecording: Boolean
        get() = verseState in activeStates
}