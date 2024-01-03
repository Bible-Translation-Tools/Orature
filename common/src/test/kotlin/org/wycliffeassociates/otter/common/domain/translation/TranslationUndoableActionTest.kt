package org.wycliffeassociates.otter.common.domain.translation

import com.jakewharton.rxrelay2.BehaviorRelay
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import java.io.File

class TranslationUndoableActionTest {
    private val chunk = mockk<Chunk>()
    private val audio = mockk<AssociatedAudio>()
    private val take = mockk<Take>()

    @Before
    fun setUp() {
        every { take.file } returns
            mockk<File> {
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

        recordAction.undo() // "delete" the current take and select the old take

        verify(exactly = 1) { deleteTimestampRelay.accept(any()) }
        verify(exactly = 1) { audio.selectTake(oldSelectedTake) }
        verify(exactly = 0) { audio.selectTake(take) }

        recordAction.redo() // bring back the original take and re-select it

        verify { audio.getAllTakes() }
        verify(exactly = 1) { deleteTimestampRelay.accept(DateHolder.empty) }
        verify(exactly = 1) { audio.selectTake(take) }
    }

    @Test
    fun testDeleteAction() {
        val deleteAction =
            TranslationTakeDeleteAction(chunk, take, true) { _, _ ->
                // mock callback
            }
        val deleteTimestampRelay = mockk<BehaviorRelay<DateHolder>>()

        every { deleteTimestampRelay.accept(any()) } returns Unit
        every { take.deletedTimestamp } returns deleteTimestampRelay

        deleteAction.execute()

        verify(exactly = 1) { deleteTimestampRelay.accept(any()) }
        verify(exactly = 0) { audio.selectTake(take) }

        deleteAction.undo() // bring back the take and re-select it if it has been selected

        verify { audio.getAllTakes() }
        verify(exactly = 1) { deleteTimestampRelay.accept(DateHolder.empty) }
        verify(exactly = 1) { audio.selectTake(take) }

        deleteAction.redo()

        verify(exactly = 3) { deleteTimestampRelay.accept(any()) }
    }

    @Test
    fun testSelectAction() {
        val oldSelectedTake = mockk<Take>()
        val selectAction = TranslationTakeSelectAction(chunk, take, oldSelectedTake)

        selectAction.execute()

        verify(exactly = 1) { audio.selectTake(take) }

        selectAction.undo()

        verify(exactly = 1) { audio.selectTake(oldSelectedTake) }

        selectAction.redo()

        verify(exactly = 2) { audio.selectTake(take) }
    }

    @Test
    fun testApproveAction() {
        val checkingRelay = mockk<BehaviorRelay<TakeCheckingState>>()
        every { checkingRelay.accept(any()) } returns Unit
        every { take.checkingState } returns checkingRelay
        every { take.checksum() } returns ""

        val oldChecking = mockk<TakeCheckingState>()
        val approveAction = TranslationTakeApproveAction(take, CheckingStatus.PEER_EDIT, oldChecking)

        approveAction.execute()

        verify(exactly = 1) { checkingRelay.accept(any()) }

        approveAction.undo()

        verify(exactly = 1) { checkingRelay.accept(oldChecking) }

        approveAction.redo()

        verify(exactly = 3) { checkingRelay.accept(any()) }
    }
}
