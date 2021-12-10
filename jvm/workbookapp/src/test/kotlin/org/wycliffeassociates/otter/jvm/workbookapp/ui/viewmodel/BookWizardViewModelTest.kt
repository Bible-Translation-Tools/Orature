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
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.notifyListenerExecuted
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.waitForListenerExecution
import tornadofx.*
import kotlin.concurrent.thread

class BookWizardViewModelTest {
    private val vm: BookWizardViewModel
    private val mockCollectionRepo = mock(ICollectionRepository::class.java)
    private val mockWorkbookRepo = mock(IWorkbookRepository::class.java)

    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }
        vm = find()
        vm.collectionRepo = mockCollectionRepo
        vm.workbookRepo = mockWorkbookRepo
    }

    private val simpleResource = Collection(
        1,
        "ulb",
        "test-label",
        "test-key",
        null,
        null,
        1
    )

    private val simpleBooks = listOf(
        Collection(
            1,
            "gen",
            "Genesis-test-label",
            "Genesis-test-title",
            null,
            null,
            1
        )
    )

    @Test
    fun loadBooksViewData() {
        `when`(mockCollectionRepo.getChildren(simpleResource))
            .thenReturn(Single.just(simpleBooks))
        val lockObject = Object()

        assertEquals(0, vm.filteredBooks.size)

        vm.filteredBooks.onChange {
            thread {
                notifyListenerExecuted(lockObject)
            }
        }
        vm.selectedSourceProperty.set(simpleResource)

        waitForListenerExecution(lockObject) {
            assertEquals(1, vm.filteredBooks.size)
            verify(mockCollectionRepo).getChildren(simpleResource)
        }
    }

    private val sourceLanguage = Language(
        "en-test",
        "English-test",
        "English-test",
        "ltr",
        true,
        "NA",
        1
    )

    private val targetLanguage = Language(
        "ar",
        "عربي",
        "Arabic-test",
        "rtl",
        true,
        "AS",
        2
    )

    private fun setUpMocks_loadExistingProject() {
        val mockTranslationModel = mock(TranslationCardModel::class.java)
        `when`(mockTranslationModel.sourceLanguage)
            .thenReturn(sourceLanguage)
        `when`(mockTranslationModel.targetLanguage)
            .thenReturn(targetLanguage)
        `when`(mockTranslationModel.modifiedTs)
            .thenReturn(null)
        vm.translationProperty.set(mockTranslationModel)

        val mockWorkbook = mock(Workbook::class.java)
        `when`(mockWorkbookRepo.getProjects(any()))
            .thenReturn(Single.just(listOf(mockWorkbook)))
    }

    @Test
    fun loadExistingProjects() {
        setUpMocks_loadExistingProject()

        assertEquals(0, vm.existingBooks.size)

        val lockObject = Object()
        vm.existingBooks.onChange {
            thread {
                notifyListenerExecuted(lockObject)
            }
        }
        vm.loadExistingProjects()

        waitForListenerExecution(lockObject) {
            assertEquals(1, vm.existingBooks.size)
            verify(mockWorkbookRepo).getProjects(any())
        }
    }

    private val rootSources = listOf(
        Collection(
            1,
            "ulb",
            "test",
            "test",
            mock(ResourceMetadata::class.java),
            null,
            10
        ),
        Collection(
            2,
            "obs",
            "test",
            "test",
            mock(ResourceMetadata::class.java),
            null,
            20
        )
    )

    private fun setUpMock_loadResources() {
        `when`(rootSources[0].resourceContainer!!.language)
            .thenReturn(sourceLanguage)
        `when`(rootSources[1].resourceContainer!!.language)
            .thenReturn(targetLanguage)
        `when`(mockCollectionRepo.getRootSources())
            .thenReturn(Single.just(rootSources))
    }

    @Test
    fun loadResources() {
        setUpMock_loadResources()

        val mockTranslationModel = mock(TranslationCardModel::class.java)
        `when`(mockTranslationModel.sourceLanguage).thenReturn(sourceLanguage)
        vm.translationProperty.set(mockTranslationModel)

        val spyVM = spy(vm)
        doNothing().`when`(spyVM).setFilterMenu()

        assertEquals(0, spyVM.sourceCollections.size)

        val lockObject = Object()
        spyVM.sourceCollections.onChange {
            thread {
                notifyListenerExecuted(lockObject)
            }
        }
        spyVM.loadResources()

        waitForListenerExecution(lockObject) {
            verify(mockTranslationModel, atLeastOnce()).sourceLanguage
            verify(spyVM).setFilterMenu()
            verify(rootSources[0].resourceContainer)!!.language
            verify(rootSources[1].resourceContainer)!!.language

            assertEquals(
                "There should be only 1 resource matching $sourceLanguage.",
                1,
                spyVM.sourceCollections.size
            )
        }
    }
}