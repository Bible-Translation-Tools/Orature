/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
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

            val viewModel = find<ExportChapterViewModel>()
            vm = spy(viewModel)
            doNothing().`when`(vm).saveContributors()
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