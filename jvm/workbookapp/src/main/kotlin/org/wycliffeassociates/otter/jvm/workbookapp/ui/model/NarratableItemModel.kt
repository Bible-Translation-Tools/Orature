package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarratableItem
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.TeleprompterItemState


class NarratableItemModel(
    private var narratableItem: NarratableItem,
    val chunk: Chunk,
    var marker: AudioMarker?,
    var previousChunksRecorded: Boolean = false,
) {
    override fun toString(): String {
        return "${chunk.sort}, ${narratableItem.verseState}, $previousChunksRecorded"
    }

    val hasRecording: Boolean
        get() = narratableItem.hasRecording

    val isActiveRecording: Boolean
        get() = narratableItem.isActiveRecording

    val isPlayOptionEnabled: Boolean
        get() = narratableItem.isPlayOptionEnabled

    val isEditVerseOptionEnabled: Boolean
        get() = narratableItem.isEditVerseOptionEnabled

    val isRecordAgainOptionEnabled: Boolean
        get() = narratableItem.isRecordAgainOptionEnabled

    var verseState: TeleprompterItemState = narratableItem.verseState
        get() = narratableItem.verseState
        set(value) {
            narratableItem.verseState = value
            field = value
        }
}



