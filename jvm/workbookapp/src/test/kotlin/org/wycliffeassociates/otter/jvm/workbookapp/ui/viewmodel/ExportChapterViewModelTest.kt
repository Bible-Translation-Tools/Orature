package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import tornadofx.FX
import tornadofx.find
import tornadofx.observableListOf
import tornadofx.onChange

class ExportChapterViewModelTest {

    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var vm: ExportChapterViewModel

        private val contributors = observableListOf(
            Contributor("test user 1"),
            Contributor("test user 2")
        )
        private val mockWorkbookPageVM = mock<WorkbookPageViewModel>() {
            on { contributors } doReturn (contributors)
        }

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }
            FX.getComponents()[WorkbookPageViewModel::class] = mockWorkbookPageVM

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
    fun loadContributors() {
        synchronized(contributors) {
            vm.contributors.clear()
            assertEquals(0, vm.contributors.size)

            vm.loadContributors()
            assertEquals(contributors.size, vm.contributors.size)
        }

    }

    @Test
    fun addContributor() {
        val name = "New test contributor"

        synchronized(contributors) {
            vm.contributors.clear()
            assertEquals(0, vm.contributors.size)

            vm.addContributor(name)
            assertEquals(1, vm.contributors.size)
            assertEquals(name, vm.contributors[0].name)
        }
    }

    @Test
    fun editContributor() {
        val data = ContributorCellData(0, "Edited name")

        synchronized(contributors) {
            vm.loadContributors()
            vm.editContributor(data)

            assertEquals(
                data.name,
                vm.contributors[0].name
            )
        }
    }

    @Test
    fun removeContributor() {
        synchronized(contributors) {
            vm.loadContributors()
            assertEquals(2, vm.contributors.size)

            vm.removeContributor(0)
            assertEquals(1, vm.contributors.size)
        }
    }
}