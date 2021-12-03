package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Language
import tornadofx.*

class LanguageSelectionViewModelTest : ViewModel() {
    private val vm: LanguageSelectionViewModel
    private val languages = initLanguages()

    init {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication {
            TestApp()
        }
        vm = LanguageSelectionViewModel(languages.toObservable())
    }

    private val queryFilterTestCases = mapOf(
        "" to 4,
        "test" to 3,
        "en" to 2,
        "ar" to 1,
        "x" to 0
    )

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

    private val regionFilterTestCases = mapOf<List<String>, Int>(
//        listOf<String>() to 4,
        listOf("NA", "AS") to 4,
        listOf("NA") to 2,
        listOf("AS") to 2,
        listOf("EU", "AS") to 2,
        listOf("EU") to 0
    )

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
        assertEquals(2, vm.filteredLanguages.size)

        vm.resetFilter()
        assertEquals(0, vm.filteredLanguages.size)
        assertEquals(0, vm.selectedRegions.size)
        assertEquals("", vm.searchQueryProperty.value)
        assertEquals(false, vm.anglicizedProperty.value)
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

    companion object {
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
    }
}