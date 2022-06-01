/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import io.reactivex.Observable
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import tornadofx.*
import java.io.File
import java.time.LocalDateTime
import javax.inject.Provider

class WorkbookPageViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var vm: WorkbookPageViewModel
        private val mockWorkbookDS = mock(WorkbookDataStore::class.java)
        private val chapters = initChapters()
        private val mockBook = Book(
            1,
            1,
            "gen",
            "test",
            "test",
            Observable.fromIterable(chapters),
            mock(ResourceMetadata::class.java),
            listOf(),
            LocalDateTime.now(),
            listOf()
        )

        private fun createWorkbookDS(): WorkbookDataStore {
            val mockWorkbook = mock(Workbook::class.java)
            `when`(mockWorkbook.target).thenReturn(mockBook)

            val mockArtworkAccessor = mock(ArtworkAccessor::class.java)
            `when`(mockArtworkAccessor.getArtwork(any()))
                .thenReturn(null)
            `when`(mockWorkbook.artworkAccessor)
                .thenReturn(mockArtworkAccessor)

            `when`(mockWorkbookDS.workbook)
                .thenReturn(mockWorkbook)
            `when`(mockWorkbookDS.activeChapterProperty)
                .thenReturn(SimpleObjectProperty())
            `when`(mockWorkbookDS.activeResourceMetadata)
                .thenReturn(mockBook.resourceMetadata)
            `when`(mockWorkbookDS.activeProjectFilesAccessor)
                .thenReturn(mock(ProjectFilesAccessor::class.java))
            `when`(mockWorkbookDS.activeProjectFilesAccessorProperty)
                .thenReturn(SimpleObjectProperty())

            return mockWorkbookDS
        }

        private fun initChapters() = listOf(
            Chapter(
                1,
                "test",
                "test",
                mock(AssociatedAudio::class.java),
                listOf(),
                listOf(),
                Observable.fromIterable(listOf())
            ),
            Chapter(
                2,
                "test",
                "test",
                mock(AssociatedAudio::class.java),
                listOf(),
                listOf(),
                Observable.fromIterable(listOf())
            )
        )

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            setInScope(createWorkbookDS(), FX.defaultScope)
            vm = find()
        }

        @AfterClass
        @JvmStatic fun tearDown() {
            FxToolkit.hideStage()
            FxToolkit.cleanupStages()
            FxToolkit.cleanupApplication(testApp)
        }
    }

    @Test
    fun openWorkbook_loadChapters() {
        assertEquals(0, vm.chapters.size)

        val lockObject = Object()
        val chapterSizeChanges = mutableListOf<Int>()
        vm.chapters.onChange {
            chapterSizeChanges.add(vm.chapters.size)
        }
        vm.openWorkbook()

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, vm.chapters.size)
        verify(mockWorkbookDS, atLeastOnce()).workbook
        verify(mockWorkbookDS).activeChapterProperty
    }

    @Test
    fun getAllBookResources() {
        assertEquals(1, vm.getAllBookResources().size)
    }

    @Test
    fun exportWorkbook() {
        val mockProjectExporter = mock(BackupProjectExporter::class.java)
        `when`(mockProjectExporter.export(any(), any(), any(), any()))
            .thenReturn(Single.just(ExportResult.SUCCESS))
        val exportProvider: Provider<BackupProjectExporter> = Provider {
            mockProjectExporter
        }
        vm.exportBackupProvider = exportProvider

        val projectTitleChanges = mutableListOf<String?>()
        val showProgressChanges = mutableListOf<Boolean>()

        vm.activeProjectTitleProperty.onChange {
            projectTitleChanges.add(it)
        }
        vm.showExportProgressDialogProperty.onChange {
            showProgressChanges.add(it)
        }
        vm.exportWorkbook(mock(File::class.java), ExportOption.BACKUP)

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(mockBook.title, projectTitleChanges[0])
        assertNull(projectTitleChanges[1])
        assertTrue(showProgressChanges[0])
        assertFalse(showProgressChanges[1])
        verify(mockProjectExporter).export(any(), any(), any(), any())
        verify(mockWorkbookDS, atLeastOnce()).workbook
        verify(mockWorkbookDS).activeResourceMetadata
        verify(mockWorkbookDS).activeProjectFilesAccessor
    }
}
