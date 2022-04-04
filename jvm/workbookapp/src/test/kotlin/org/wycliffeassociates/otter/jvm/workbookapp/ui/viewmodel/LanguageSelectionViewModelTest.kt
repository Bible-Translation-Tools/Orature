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

import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Language
import tornadofx.*

class LanguageSelectionViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var vm: LanguageSelectionViewModel
        private val languages = initLanguages()

        private val queryFilterTestCases = mapOf(
            "" to 4,
            "test" to 3,
            "en" to 2,
            "ar" to 1,
            "x" to 0
        )

        private val regionFilterTestCases = mapOf(
            listOf<String>() to 4,
            listOf("NA", "AS") to 4,
            listOf("NA") to 2,
            listOf("AS") to 2,
            listOf("EU", "AS") to 2,
            listOf("EU") to 0
        )

        fun initLanguages(): List<Language> {
            return listOf(
                Language(
                    "en",
                    "English",
                    "English",
                    "ltr",
                    true,
                    "NA",
                    0
                ),
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
                    "AS",
                    2
                ),
                Language(
                    "vi-test",
                    "Vietnamese",
                    "Vietnamese",
                    "ltr",
                    true,
                    "AS",
                    3
                )
            )
        }

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            vm = LanguageSelectionViewModel(languages.toObservable())
        }

        @AfterClass
        @JvmStatic fun tearDown() {
            FxToolkit.hideStage()
            FxToolkit.cleanupStages()
            FxToolkit.cleanupApplication(testApp)
        }
    }

    @Test
    fun searchQueryFilter() {
        queryFilterTestCases.forEach { (key, value) ->
            vm.searchQueryProperty.set(key)
            assertEquals(
                "Search query: \"$key\".",
                value,
                vm.filteredLanguages.size
            )
        }
    }

    @Test
    fun regionFilter() {
        regionFilterTestCases.forEach { (key, value) ->
            vm.selectedRegions.setAll(key)
            assertEquals(
                "Selected region(s): \"$key\".",
                value,
                vm.filteredLanguages.size
            )
        }
    }

    @Test
    fun resetFilter() {
        vm.searchQueryProperty.set("en")
        vm.selectedRegions.setAll("NA", "AS")

        vm.resetFilter()
        assertEquals(0, vm.selectedRegions.size)
        assertEquals(0, vm.regions.size)
        assertEquals("", vm.searchQueryProperty.value)
        assertFalse(vm.anglicizedProperty.value)
    }

    @Test
    fun setFilterMenu() {
        val defaultItemCount = 3 // two separators and one filter for anglicized
        vm.setFilterMenu()
        assertEquals(defaultItemCount, vm.menuItems.size)

        val regions = languages.distinctBy { l -> l.region }.map{ l -> l.region }
        vm.regions.setAll(regions)
        vm.setFilterMenu()
        val menuItemCount = defaultItemCount + regions.size
        assertEquals(menuItemCount, vm.menuItems.size)
    }
}
