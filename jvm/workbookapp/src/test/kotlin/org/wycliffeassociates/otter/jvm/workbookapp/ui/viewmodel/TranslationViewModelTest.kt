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

import io.reactivex.Single
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.CreateTranslation
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import tornadofx.*

class TranslationViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var vm: TranslationViewModel

        private val sourceLanguage = Language(
            "en-test",
            "English-test",
            "English-test",
            "ltr",
            true,
            "NA",
            1
        )

        private val rcMetadata = mock(ResourceMetadata::class.java).apply {
            `when`(this.language).thenReturn(sourceLanguage)
        }

        private val collections = listOf(
            Collection(
                1,
                "ulb",
                "test-label",
                "test-key",
                rcMetadata,
                null,
                1
            )
        )

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

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
    fun loadSourceLanguages() {
        val mockCollectionRepo = mock(ICollectionRepository::class.java)
        `when`(
            mockCollectionRepo.getRootSources()
        ).thenReturn(
            Single.just(collections)
        )
        vm.collectionRepo = mockCollectionRepo

        assertEquals(0, vm.sourceLanguages.size)

        vm.loadSourceLanguages()

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, vm.sourceLanguages.size)
        verify(mockCollectionRepo).getRootSources()
        verify(rcMetadata, atLeastOnce()).language
    }

    @Test
    fun loadTargetLanguages() {
        val languages = LanguageSelectionViewModelTest.initLanguages()
        val translation = Translation(sourceLanguage, languages[0], null)

        val mockLanguageRepo = mock(ILanguageRepository::class.java)
        `when`(mockLanguageRepo.getAll())
            .thenReturn(Single.just(languages))
        `when`(mockLanguageRepo.getAllTranslations())
            .thenReturn(Single.just(listOf(translation)))
        vm.languageRepo = mockLanguageRepo

        assertEquals(0, vm.targetLanguages.size)

        vm.loadTargetLanguages()

        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(languages.size, vm.targetLanguages.size)
        verify(mockLanguageRepo).getAll()
        verify(mockLanguageRepo).getAllTranslations()
    }
}
