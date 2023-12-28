package org.wycliffeassociates.otter.common.domain.translation

import com.jakewharton.rxrelay2.BehaviorRelay
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import java.io.File

class TranslationUndoableActionTest {

    private val chunk = mockk<Chunk>()
    private val audio = mockk<AssociatedAudio>()
    private val take = mockk<Take>()

    @Before
    fun setUp() {
        every { take.file } returns mockk<File> {
            every { setLastModified(any()) } returns true
        }
        every { audio.getAllTakes() } returns arrayOf(take)
        every { audio.selectTake(any()) } returns Unit
        every { audio.insertTake(any()) } returns Unit
        every { chunk.audio } returns audio
    }

    @Test
    fun testRecordAction() {
        val oldSelectedTake = mockk<Take>()
        val recordAction = TranslationTakeRecordAction(chunk, take, oldSelectedTake)
        val deleteTimestampRelay = mockk<BehaviorRelay<DateHolder>>()

        every { deleteTimestampRelay.accept(any()) } returns Unit
        every { take.deletedTimestamp } returns deleteTimestampRelay

        verify(exactly = 0) { audio.insertTake(take) }

        recordAction.execute() // inserts the newly recorded take

        verify(exactly = 1) { audio.insertTake(take) }

        recordAction.undo() // delete the current take and select the old take

        verify(exactly = 1) { deleteTimestampRelay.accept(any()) }
        verify(exactly = 1) { audio.selectTake(oldSelectedTake) }

        recordAction.redo() // bring back the original take and re-select it

        verify { audio.getAllTakes() }
        verify { deleteTimestampRelay.accept(DateHolder.empty) }
        verify(exactly = 1) { audio.selectTake(take) }
    }
}