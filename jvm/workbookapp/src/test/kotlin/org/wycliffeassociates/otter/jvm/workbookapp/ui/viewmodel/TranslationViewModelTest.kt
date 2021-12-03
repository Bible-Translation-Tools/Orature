package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Single
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateTranslation
import tornadofx.ViewModel
import tornadofx.onChangeOnce
import tornadofx.onChangeTimes

class TranslationViewModelTest : ViewModel() {
    private val testApp: TestApp = TestApp()
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
            testApp
        }
        vm = find()
    }

    @Test
    fun createTranslation_showHideProgress() {
        val mockCreateTranslation = mock(CreateTranslation::class.java)
        `when`(mockCreateTranslation.create(sourceLanguage, targetLanguage))
            .thenReturn(Single.just(1))

        vm.creationUseCase = mockCreateTranslation

        val progressStatus = mutableListOf<Boolean>()
        // progress should be true and then false
        vm.showProgressProperty.onChangeTimes(2) {
            it?.let {
                progressStatus.add(it)
            }
        }

        vm.selectedTargetLanguageProperty.onChangeOnce {
            verify(mockCreateTranslation).create(sourceLanguage, targetLanguage)
            assertTrue(progressStatus[0])
            assertFalse(progressStatus[1])
        }
        vm.selectedSourceLanguageProperty.onChangeOnce {
            vm.selectedTargetLanguageProperty.set(targetLanguage)
        }
        vm.selectedSourceLanguageProperty.set(sourceLanguage)
    }
}