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
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import tornadofx.*
import kotlin.concurrent.thread

class HomePageViewModelTest {
    private val vm: HomePageViewModel
    private val mockPreferenceRepo = mock(IAppPreferencesRepository::class.java)
    private val mockCollectionRepo = mock(ICollectionRepository::class.java)
    private val mockWorkbookRepo = mock(IWorkbookRepository::class.java)
    private val mockSettingsVM = mock(SettingsViewModel::class.java)
    private val mockLanguageRepo = mock(ILanguageRepository::class.java)

    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }

        `when`(mockSettingsVM.refreshPlugins()).then { }
        setInScope(mockSettingsVM, FX.defaultScope)

        vm = find(FX.defaultScope)
        vm.preferencesRepository = mockPreferenceRepo
        vm.collectionRepo = mockCollectionRepo
        vm.workbookRepo = mockWorkbookRepo
        vm.languageRepository = mockLanguageRepo
    }

    private val resumeProjectId = 1
    private val collection = Collection(
        1,
        "gen",
        "Genesis-test-label",
        "Genesis-test-title",
        null,
        null,
        resumeProjectId
    )

    private fun setUpMocks_loadResumeBook() {
        `when`(mockPreferenceRepo.resumeProjectId())
            .thenReturn(Single.just(resumeProjectId))

        `when`(mockCollectionRepo.getProject(resumeProjectId))
            .thenReturn(Maybe.just(collection))
    }

    @Test
    fun loadResumeBook() {
        setUpMocks_loadResumeBook()
        val mockWorkbook = mock(Workbook::class.java)
        `when`(mockWorkbookRepo.getWorkbook(collection))
            .thenReturn(Maybe.just(mockWorkbook))

        val lockObject = Object()
        vm.resumeBookProperty.onChange {
            if (it != null) {
                thread {
                    notifyListenerExecuted(lockObject)
                }
            }
        }
        vm.loadResumeBook()

        waitForListenerExecution(lockObject) {
            assertNotNull(vm.resumeBookProperty.value)
            assertEquals(mockWorkbook, vm.resumeBookProperty.value)
            verify(mockPreferenceRepo).resumeProjectId()
            verify(mockCollectionRepo).getProject(resumeProjectId)
        }
        verify(mockSettingsVM).refreshPlugins()
    }

    private val languages = listOf(
        Language(
            "en-test",
            "English-test",
            "English-test",
            "ltr",
            true,
            "NA",
            1
        ),
        Language(
            "ar-test",
            "Arabic-test",
            "Arabic-test",
            "rtl",
            true,
            "ME",
            2
        )
    )

    private val translations = listOf(
        Translation(languages[0], languages[1], null, 0),
        Translation(languages[1], languages[0], null, 1)
    )

    @Test
    fun loadTranslation() {
        val mockProject = mock(Workbook::class.java)
        `when`(mockWorkbookRepo.getProjects(any()))
            .thenReturn(Single.just(listOf(mockProject)))
        `when`(mockLanguageRepo.getAllTranslations())
            .thenReturn(Single.just(translations))

        assertEquals(0, vm.translations.size)
        assertEquals(0, vm.translationModels.size)

        val lockObject = Object()
        vm.translationModels.onChange {
            thread {
                notifyListenerExecuted(lockObject)
            }
        }
        vm.loadTranslations()

        waitForListenerExecution(lockObject) {
            assertEquals(2, vm.translations.size)
            assertEquals(2, vm.translationModels.size)
            verify(mockWorkbookRepo, atLeastOnce()).getProjects(any())
            verify(mockLanguageRepo).getAllTranslations()
        }
    }
}
