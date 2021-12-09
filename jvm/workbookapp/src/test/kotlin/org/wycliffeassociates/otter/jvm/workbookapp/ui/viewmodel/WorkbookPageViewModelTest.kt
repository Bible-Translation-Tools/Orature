/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import io.reactivex.Observable
import javafx.beans.property.SimpleObjectProperty
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.notifyListenerExecuted
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.waitForListenerExecution
import tornadofx.*
import java.time.LocalDateTime

class WorkbookPageViewModelTest {
    private val vm: WorkbookPageViewModel
    private val mockWorkbookDS = mock(WorkbookDataStore::class.java)
    private val chapters = initChapters()
    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }

        setInScope(createWorkbookDS(), FX.defaultScope)
        vm = find()
    }

    private fun createWorkbookDS(): WorkbookDataStore {
        val mockBook = Book(
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

        return mockWorkbookDS
    }

    @Test
    fun openWorkbook() {
        assertEquals(0, vm.chapters.size)

        val lockObject = Object()
        vm.chapters.onChange {
            if (vm.chapters.size > 0) {
                notifyListenerExecuted(lockObject)
            }
        }
        vm.openWorkbook()

        waitForListenerExecution(lockObject) {
            assertEquals(
                "Opened workbook - chapter model must have " +
                        "${chapters.size} chapter(s) + 1 workbook banner.",
                chapters.size + 1,
                vm.chapters.size)
        }

        verify(mockWorkbookDS, atLeastOnce()).workbook
        verify(mockWorkbookDS).activeChapterProperty
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
}