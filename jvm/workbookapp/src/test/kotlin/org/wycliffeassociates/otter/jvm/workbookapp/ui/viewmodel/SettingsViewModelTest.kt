package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import tornadofx.*

class SettingsViewModelTest : ViewModel() {

    private val testApp: TestApp = TestApp()
    private val settingsViewModel: SettingsViewModel

    private val recorder = AudioPluginData(
        0,
        "Recorder",
        "1",
        false,
        true,
        false,
        "path_to_executable",
        listOf(),
        null
    )
    private val editor = AudioPluginData(
        0,
        "Editor",
        "1",
        true,
        false,
        false,
        "path_to_executable",
        listOf(),
        null
    )

    init {
        FX.setApplication(FX.defaultScope, testApp)
        settingsViewModel = find()
    }

    @Test
    fun selectRecorder_setsSelectedRecorderProperty() {
        settingsViewModel.selectRecorder(recorder)

        Assert.assertEquals(recorder, settingsViewModel.selectedRecorderProperty.value)
    }

    @Test
    fun selectEditor_setsSelectedEditorProperty() {
        settingsViewModel.selectEditor(editor)

        Assert.assertEquals(editor, settingsViewModel.selectedEditorProperty.value)
    }
}
