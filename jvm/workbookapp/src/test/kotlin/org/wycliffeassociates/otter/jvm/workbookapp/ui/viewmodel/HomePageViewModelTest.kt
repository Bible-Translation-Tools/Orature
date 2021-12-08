package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.*
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.notifyListenerExecuted
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.Utilities.Companion.waitForListenerExecution
import tornadofx.*

class HomePageViewModelTest {
    private val vm: HomePageViewModel
    private val mockPreferenceRepo = mock(IAppPreferencesRepository::class.java)
    private val mockCollectionRepo = mock(ICollectionRepository::class.java)
    private val mockWorkbookRepo = mock(IWorkbookRepository::class.java)
    private val mockSettingsVM = mock(SettingsViewModel::class.java)

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
            notifyListenerExecuted(lockObject)
        }
        vm.loadResumeBook()

        waitForListenerExecution(lockObject) {
            assertNotNull(vm.resumeBookProperty.value)
            assertEquals(mockWorkbook, vm.resumeBookProperty.value)
        }

        verify(mockSettingsVM).refreshPlugins()
        verify(mockWorkbook)
    }
}