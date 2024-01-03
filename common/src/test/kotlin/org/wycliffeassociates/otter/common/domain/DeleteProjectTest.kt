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
        every { deleteSpy.deleteProjects(bookList) } returns Completable.complete()

        val normalDelete1 =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        val normalDelete2 =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        verify(exactly = 0) { deleteSpy.deleteProjects(bookList) }

        Thread.sleep(1000) // wait until finishes

        verify(exactly = 2) { deleteSpy.deleteProjects(bookList) }
    }

    @Test
    fun testCancelDeleteBeforeTimeout() {
        val deleteSpy = spyk(deleteUseCase)
        val bookList = listOf(mockk<WorkbookDescriptor>())
        every { deleteSpy.deleteProjects(bookList) } returns Completable.complete()

        val cancellingDelete =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        val normalDelete =
            deleteSpy
                .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
                .subscribe()

        verify(exactly = 0) { deleteSpy.deleteProjects(bookList) }

        cancellingDelete.dispose() // cancel before timeout

        Assert.assertTrue(cancellingDelete.isDisposed)
        Assert.assertFalse(normalDelete.isDisposed)

        Thread.sleep(600)

        verify(exactly = 1) { deleteSpy.deleteProjects(bookList) }
    }
}
