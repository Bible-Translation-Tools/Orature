package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.nhaarman.mockitokotlin2.any
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import tornadofx.*

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

    // TODO: assertion in onChange()'s body not executed
    @Test
    fun loadBooksViewData() {
        `when`(mockCollectionRepo.getChildren(simpleResource))
            .thenReturn(Single.just(simpleBooks))

        assertEquals(0, vm.filteredBooks.size)

        vm.filteredBooks.onChange {
            assertEquals(1, vm.filteredBooks.size)
            println("books loaded - verify")
        }
        vm.selectedSourceProperty.set(simpleResource)

        verify(mockCollectionRepo).getChildren(simpleResource)
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
        val mockTranslation = mock(TranslationCardModel::class.java)
        `when`(mockTranslation.sourceLanguage)
            .thenReturn(sourceLanguage)
        `when`(mockTranslation.targetLanguage)
            .thenReturn(targetLanguage)
        `when`(mockTranslation.modifiedTs)
            .thenReturn(null)
        vm.translationProperty.set(mockTranslation)

        val mockWorkbook = mock(Workbook::class.java)
        `when`(mockWorkbookRepo.getProjects(any()))
            .thenReturn(Single.just(listOf(mockWorkbook)))
    }
    @Test
    fun loadExistingProjects() {
        setUpMocks_loadExistingProject()

        assertEquals(0, vm.existingBooks.size)

        vm.existingBooks.onChange {
            assertEquals(1, vm.existingBooks.size)
        }
        vm.loadExistingProjects()

        verify(mockWorkbookRepo).getProjects(any())
    }
}