///**
// * Copyright (C) 2020, 2021 Wycliffe Associates
// *
// * This file is part of Orature.
// *
// * Orature is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Orature is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
// */
//package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel
//
//import org.junit.Assert
//import org.junit.Test
//import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
//import tornadofx.*
//
//class SettingsViewModelTest : ViewModel() {
//
//    private val testApp: TestApp = TestApp()
//    private val settingsViewModel: SettingsViewModel
//
//    private val recorder = AudioPluginData(
//        0,
//        "Recorder",
//        "1",
//        false,
//        true,
//        false,
//        "path_to_executable",
//        listOf(),
//        null
//    )
//    private val editor = AudioPluginData(
//        0,
//        "Editor",
//        "1",
//        true,
//        false,
//        false,
//        "path_to_executable",
//        listOf(),
//        null
//    )
//
//    init {
//        FX.setApplication(FX.defaultScope, testApp)
//        settingsViewModel = find()
//    }
//
//    @Test
//    fun selectRecorder_setsSelectedRecorderProperty() {
//        settingsViewModel.selectRecorder(recorder)
//
//        Assert.assertEquals(recorder, settingsViewModel.selectedRecorderProperty.value)
//    }
//
//    @Test
//    fun selectEditor_setsSelectedEditorProperty() {
//        settingsViewModel.selectEditor(editor)
//
//        Assert.assertEquals(editor, settingsViewModel.selectedEditorProperty.value)
//    }
//}
