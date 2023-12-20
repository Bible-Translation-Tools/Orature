package org.wycliffeassociates.otter.common.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
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
        val collectionRepo = mock<ICollectionRepository>()
        val workbookRepo = mock<IWorkbookRepository>()
        val workbookDescriptorRepo = mock<IWorkbookDescriptorRepository>()
        val directoryProvider = mock<IDirectoryProvider>()

        deleteUseCase = DeleteProject(collectionRepo, directoryProvider, workbookRepo, workbookDescriptorRepo)
    }

    @Test
    fun testDeleteWithTimer() {
        val deleteSpy = spy(deleteUseCase)
        val bookList = listOf(mock<WorkbookDescriptor>())
        doReturn(Completable.complete()).whenever(deleteSpy).deleteProjects(bookList)

        val delete1 = deleteSpy
            .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
            .subscribe()

        val delete2 = deleteSpy
            .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
            .subscribe()

        verify(deleteSpy, never()).deleteProjects(bookList)

        Thread.sleep(1000) // wait until finishes

        verify(deleteSpy, times(2)).deleteProjects(bookList)
    }

    @Test
    fun testCancelDeleteBeforeTimeout() {
        val deleteSpy = spy(deleteUseCase)
        val bookList = listOf(mock<WorkbookDescriptor>())
        doReturn(Completable.complete()).whenever(deleteSpy).deleteProjects(bookList)

        val delete1 = deleteSpy
            .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
            .subscribe()

        val delete2 = deleteSpy
            .deleteProjectsWithTimer(bookList, timeoutMillis = 500)
            .subscribe()

        verify(deleteSpy, never()).deleteProjects(bookList)

        delete1.dispose() // cancel before timeout
        Thread.sleep(600)

        verify(deleteSpy, times(1)).deleteProjects(bookList)
    }
}