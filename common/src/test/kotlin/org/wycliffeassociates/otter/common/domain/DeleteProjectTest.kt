package org.wycliffeassociates.otter.common.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.util.concurrent.atomic.AtomicInteger

class DeleteProjectTest {
    private lateinit var deleteUseCase: DeleteProject

    @Before
    fun setup() {
        val collectionRepo = mockk<ICollectionRepository>()
        val workbookRepo = mockk<IWorkbookRepository>()
        val workbookDescriptorRepo = mockk<IWorkbookDescriptorRepository>()
        val directoryProvider = mockk<IDirectoryProvider>()

        deleteUseCase = DeleteProject(collectionRepo, directoryProvider, workbookRepo, workbookDescriptorRepo)
    }

    @Test
    fun testDeleteWithTimer() {
        val deleteSpy = spyk(deleteUseCase)
        val bookList = listOf(mockk<WorkbookDescriptor>())
        val deleteCounter = AtomicInteger(0)
        val deleteObservable = Completable
            .complete()
            .doOnSubscribe { deleteCounter.incrementAndGet() }

        every { deleteSpy.deleteProjects(bookList) } returns deleteObservable

        val normalDelete1 =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        val normalDelete2 =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        Assert.assertEquals(0, deleteCounter.get())

        Thread.sleep(700) // wait until finishes

        Assert.assertEquals(2, deleteCounter.get())
    }

    @Test
    fun testCancelDeleteBeforeTimeout() {
        val deleteSpy = spyk(deleteUseCase)
        val bookList = listOf(mockk<WorkbookDescriptor>())
        val deleteCounter = AtomicInteger(0)
        val deleteObservable = Completable
            .complete()
            .doOnSubscribe { deleteCounter.incrementAndGet() }

        every { deleteSpy.deleteProjects(bookList) } returns deleteObservable

        val cancellingDelete =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        val normalDelete =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        Assert.assertEquals(0, deleteCounter.get())

        cancellingDelete.dispose() // cancel before timeout

        Assert.assertTrue(cancellingDelete.isDisposed)
        Assert.assertFalse(normalDelete.isDisposed)

        Thread.sleep(700)

        Assert.assertEquals(1, deleteCounter.get())
    }
}
