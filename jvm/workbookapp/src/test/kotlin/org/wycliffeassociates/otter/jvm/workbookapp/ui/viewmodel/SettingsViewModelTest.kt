package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
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
    private val audioPlugins = listOf(recorder, editor)

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

    @Test
    fun refreshPlugins_setsRecordersList() {
        val spiedSettingsViewModel = spy(settingsViewModel)
        val spiedPluginRepository = spy(spiedSettingsViewModel.pluginRepository)

        whenever(spiedPluginRepository.getAll()).thenReturn(Single.just(audioPlugins))
        whenever(spiedSettingsViewModel.pluginRepository).thenReturn(spiedPluginRepository)

        spiedSettingsViewModel.refreshPlugins()

        Assert.assertEquals(2, spiedSettingsViewModel.audioPlugins.size)
    }
}
