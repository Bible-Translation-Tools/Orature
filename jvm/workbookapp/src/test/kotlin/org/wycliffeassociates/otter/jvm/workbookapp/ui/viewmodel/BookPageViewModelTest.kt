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

import io.reactivex.Observable
import javafx.beans.property.SimpleObjectProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.Mockito.*
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.notifyListenerExecuted
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.waitForListenerExecution
import tornadofx.*
import kotlin.concurrent.thread

class BookPageViewModelTest {
    private val vm: BookPageViewModel

    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }
        setInScope(createMockDataStore(), FX.defaultScope)
        vm = find(FX.defaultScope)
    }

    private fun createMockDataStore() : WorkbookDataStore {
        val mockWorkbookDS = mock(WorkbookDataStore::class.java)
        `when`(mockWorkbookDS.activeWorkbookProperty)
            .thenReturn(SimpleObjectProperty<Workbook>())
        `when`(mockWorkbookDS.activeChapterProperty)
            .thenReturn(SimpleObjectProperty())

        return mockWorkbookDS
    }

    private fun setUpMockWorkbook() : Workbook {
        val chapter = mock(Chapter::class.java)
        `when`(chapter.title).thenReturn("test")
        `when`(chapter.sort)
            .thenReturn(1)
            .thenReturn(2)

        val mockBook = mock(Book::class.java)
        `when`(mockBook.chapters)
            .thenReturn(Observable.just(chapter, chapter))

        val mockWorkbook = mock(Workbook::class.java)
        `when`(mockWorkbook.target).thenReturn(mockBook)

        return mockWorkbook
    }

    @Test
    fun loadChapters() {
        val mockWorkbook = setUpMockWorkbook()
        assertEquals(0, vm.allContent.size)

        val lockObject = Object()
        vm.allContent.onChange {
            if (vm.allContent.size != 0) {
                thread {
                    notifyListenerExecuted(lockObject)
                }
            }
        }
        vm.workbookDataStore.activeWorkbookProperty.set(mockWorkbook)

        waitForListenerExecution(lockObject) {
            assertEquals(
                "Loaded chapters count is incorrect.",
                2,
                vm.allContent.size
            )
            assertNotEquals(vm.allContent[0].sort, vm.allContent[1].sort)
        }

        verify(mockWorkbook).target
    }
}