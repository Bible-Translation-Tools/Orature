package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.CreateTranslation
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.notifyListenerExecuted
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.waitForListenerExecution
import tornadofx.*

class TranslationViewModelTest {
    private val vm: TranslationViewModel

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
        "ar-test",
        "Arabic-test",
        "Arabic-test",
        "rtl",
        true,
        "ME",
        2
    )

    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }
        vm = find()
    }

    @Test
    fun createTranslation_showHideProgress() {
        val mockCreateTranslation = mock(CreateTranslation::class.java)
        `when`(mockCreateTranslation.create(sourceLanguage, targetLanguage))
            .thenReturn(Single.just(1))

        vm.creationUseCase = mockCreateTranslation

        val lockObject = Object()
        val progressStatus = mutableListOf<Boolean>()
        // progress should be true and then false
        vm.showProgressProperty.onChangeTimes(2) {
            it?.let {
                progressStatus.add(it)
                if (progressStatus.size == 2) {
                    notifyListenerExecuted(lockObject)
                }
            }
        }

        vm.selectedSourceLanguageProperty.onChangeOnce {
            vm.selectedTargetLanguageProperty.set(targetLanguage)
        }
        vm.selectedSourceLanguageProperty.set(sourceLanguage)

        waitForListenerExecution(lockObject) {
            verify(mockCreateTranslation).create(sourceLanguage, targetLanguage)
            assertTrue(progressStatus[0])
            assertFalse(progressStatus[1])
        }
    }

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

        val lockObject = Object()
        vm.sourceLanguages.onChange {
            notifyListenerExecuted(lockObject)
        }
        vm.loadSourceLanguages()

        waitForListenerExecution(lockObject) {
            assertEquals(1, vm.sourceLanguages.size)
        }
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

        val lockObject = Object()
        vm.targetLanguages.onChange {
            notifyListenerExecuted(lockObject)
        }
        vm.loadTargetLanguages()

        waitForListenerExecution(lockObject) {
            assertEquals(languages.size, vm.targetLanguages.size)
        }

        verify(mockLanguageRepo).getAll()
        verify(mockLanguageRepo).getAllTranslations()
    }
}