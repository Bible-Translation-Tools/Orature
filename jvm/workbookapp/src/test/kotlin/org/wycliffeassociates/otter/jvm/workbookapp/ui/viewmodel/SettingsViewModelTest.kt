/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import javafx.geometry.NodeOrientation
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import tornadofx.*

class SettingsViewModelTest {

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

    private val english = Language(
        "en",
        "English",
        "English",
        "ltr",
        true,
        "Europe"
    )

    private val spanish = Language(
        "es",
        "Español",
        "Spanish",
        "ltr",
        true,
        "Europe"
    )

    private val arabic = Language(
        "ar",
        "عربي",
        "Arabic",
        "rtl",
        true,
        "Asia"
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

    @Test
    fun bind_selectedLanguageProperty() {
        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn spanish
        }

        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.bind()

        Assert.assertEquals(
            settingsViewModel.localeLanguage.preferredLanguage,
            settingsViewModel.selectedLocaleLanguageProperty.value
        )
    }

    @Test
    fun bind_supportedLanguages() {
        val localeLanguage = mock<LocaleLanguage> {
            on { supportedLanguages } doReturn listOf(english, spanish, arabic)
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.bind()

        Assert.assertTrue(settingsViewModel.supportedLocaleLanguages.count() == 3)
    }

    @Test
    fun setAppOrientation_LTR() {
        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn spanish
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.setAppOrientation()

        Assert.assertEquals(settingsViewModel.orientationProperty.value, NodeOrientation.LEFT_TO_RIGHT)
    }

    @Test
    fun setAppOrientation_RTL() {
        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn arabic
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.setAppOrientation()

        Assert.assertEquals(settingsViewModel.orientationProperty.value, NodeOrientation.RIGHT_TO_LEFT)
    }
}
