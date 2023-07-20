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

import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
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
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import tornadofx.*
import java.io.File
import java.time.LocalDateTime
import javax.inject.Provider
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class WorkbookPageViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var vm: WorkbookPageViewModel
        private val mockWorkbookDS = mock(WorkbookDataStore::class.java)
        private val chapters = initChapters()
        private val mockResourceMetadata = mock(ResourceMetadata::class.java).apply {
            `when`(identifier).thenReturn("ulb")
            `when`(language).thenReturn(mock(Language::class.java))
            `when`(creator).thenReturn("test")
        }
        private val mockBook = Book(
            1,
            1,
            "gen",
            "test",
            "test",
            Observable.fromIterable(chapters),
            mockResourceMetadata,
            listOf(),
            LocalDateTime.now(),
            listOf()
        )
        private val mockProjectFilesAccessor = mock<ProjectFilesAccessor> {
            on { getContributorInfo() } doReturn listOf(Contributor("testContributor"))
        }

        private fun createWorkbookDS(): WorkbookDataStore {
            val mockWorkbook = mock(Workbook::class.java)
            `when`(mockWorkbook.target).thenReturn(mockBook)

            `when`(mockWorkbook.projectFilesAccessor)
                .thenReturn(mockProjectFilesAccessor)

            val mockArtworkAccessor = mock(ArtworkAccessor::class.java)
            `when`(mockArtworkAccessor.getArtwork(any()))
                .thenReturn(null)
            `when`(mockWorkbook.artworkAccessor)
                .thenReturn(mockArtworkAccessor)

            `when`(mockWorkbookDS.workbook)
                .thenReturn(mockWorkbook)
            `when`(mockWorkbookDS.activeChapterProperty)
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
                lazy { ReplayRelay.create() },
                Single.just(0),
                {},
                {}
            ),
            Chapter(
                2,
                "test",
                "test",
                mock(AssociatedAudio::class.java),
                listOf(),
                listOf(),
                lazy { ReplayRelay.create() },
                Single.just(0),
                {},
                {}
            )
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            setInScope(createWorkbookDS(), FX.defaultScope)
            vm = find()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            FxToolkit.hideStage()
            FxToolkit.cleanupStages()
            FxToolkit.cleanupApplication(testApp)
        }
    }

    @Test
    fun openWorkbook_loadChapters() {
        assertEquals(0, vm.chapters.size)

        val chapterSizeChanges = mutableListOf<Int>()
        vm.chapters.onChange {
            chapterSizeChanges.add(vm.chapters.size)
        }
        vm.openWorkbook()

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, vm.chapters.size)
        verify(mockWorkbookDS, atLeastOnce()).workbook
    }

    @Test
    fun getAllBookResources() {
        assertEquals(1, vm.getAllBookResources().size)
    }

    @Test
    fun exportWorkbook() {
        val mockProjectExporter = mock(BackupProjectExporter::class.java)
        `when`(mockProjectExporter.export(any(), any(), any(), anyOrNull()))
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
        vm.exportWorkbook(mock(File::class.java), ExportType.BACKUP)

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(mockBook.title, projectTitleChanges[0])
        assertNull(projectTitleChanges[1])
        assertTrue(showProgressChanges[0])
        assertFalse(showProgressChanges[1])
        verify(mockProjectExporter).export(any(), any(), any(), anyOrNull())
        verify(mockWorkbookDS, atLeastOnce()).workbook
    }
}