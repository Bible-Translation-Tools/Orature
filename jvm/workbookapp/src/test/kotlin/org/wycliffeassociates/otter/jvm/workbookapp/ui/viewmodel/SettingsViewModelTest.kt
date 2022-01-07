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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Completable
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.NodeOrientation
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import tornadofx.*

class SettingsViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()
        private lateinit var settingsViewModel: SettingsViewModel

        private var showChangeLanguageSuccessDialogListener: ChangeListener<Boolean>? = null
        private var selectedRecorderListener: ChangeListener<AudioPluginData>? = null
        private var selectedEditorListener: ChangeListener<AudioPluginData>? = null
        private var selectedLocaleLanguageListener: ChangeListener<Language>? = null
        private var supportedLocaleLanguagesListener: ListChangeListener<Language>? = null
        private var orientationListener: ChangeListener<NodeOrientation>? = null

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

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            settingsViewModel = find()
        }

        @AfterClass
        fun tearDown() {
            FxToolkit.hideStage()
            testApp.stop()
        }
    }

    private fun <T> createChangeListener(callback: (T) -> Unit): ChangeListener<T> {
        return ChangeListener { _, _, value ->
            callback(value)
        }
    }

    private fun <T> createListChangeListener(callback: (List<T>) -> Unit): ListChangeListener<T> {
        return ListChangeListener {
            callback(it.list)
        }
    }

    @Before
    fun prepare() {
        settingsViewModel.selectedLocaleLanguageProperty.set(null)
    }

    @After
    fun cleanup() {
        showChangeLanguageSuccessDialogListener?.let {
            settingsViewModel.showChangeLanguageSuccessDialogProperty.removeListener(it)
        }
        selectedRecorderListener?.let {
            settingsViewModel.selectedRecorderProperty.removeListener(it)
        }
        selectedEditorListener?.let {
            settingsViewModel.selectedEditorProperty.removeListener(it)
        }
        selectedLocaleLanguageListener?.let {
            settingsViewModel.selectedLocaleLanguageProperty.removeListener(it)
        }
        supportedLocaleLanguagesListener?.let {
            settingsViewModel.supportedLocaleLanguages.removeListener(it)
        }
        orientationListener?.let {
            settingsViewModel.orientationProperty.removeListener(it)
        }
    }

    @Test
    fun selectRecorder_setsSelectedRecorderProperty() {
        selectedRecorderListener = createChangeListener {
            Assert.assertEquals(recorder, it)
        }
        settingsViewModel.selectedRecorderProperty.addListener(selectedRecorderListener)

        settingsViewModel.selectRecorder(recorder)
    }

    @Test
    fun selectEditor_setsSelectedEditorProperty() {
        selectedEditorListener = createChangeListener {
            Assert.assertEquals(editor, it)
        }
        settingsViewModel.selectedEditorProperty.addListener(selectedEditorListener)

        settingsViewModel.selectEditor(editor)
    }

    @Test
    fun bind_selectedLanguageProperty() {
        selectedLocaleLanguageListener = createChangeListener {
            Assert.assertEquals(settingsViewModel.localeLanguage.preferredLanguage, it)
        }
        settingsViewModel.selectedLocaleLanguageProperty.addListener(selectedLocaleLanguageListener)

        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn spanish
        }

        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.bind()
    }

    @Test
    fun bind_supportedLanguages() {
        supportedLocaleLanguagesListener = createListChangeListener {
            Assert.assertTrue(it.count() == 3)
        }
        settingsViewModel.supportedLocaleLanguages.addListener(supportedLocaleLanguagesListener)

        val localeLanguage = mock<LocaleLanguage> {
            on { supportedLanguages } doReturn listOf(english, spanish, arabic)
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.bind()
    }

    @Test
    fun setAppOrientation_LTR() {
        orientationListener = createChangeListener {
            Assert.assertEquals(NodeOrientation.LEFT_TO_RIGHT, it)
        }
        settingsViewModel.orientationProperty.addListener(orientationListener)

        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn spanish
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.setAppOrientation()
    }

    @Test
    fun setAppOrientation_RTL() {
        orientationListener = createChangeListener {
            Assert.assertEquals(NodeOrientation.RIGHT_TO_LEFT, it)
        }
        settingsViewModel.orientationProperty.addListener(orientationListener)

        val localeLanguage = mock<LocaleLanguage> {
            on { preferredLanguage } doReturn arabic
        }
        settingsViewModel.localeLanguage = localeLanguage
        settingsViewModel.setAppOrientation()
    }

    @Test
    fun updateLanguage() {
        showChangeLanguageSuccessDialogListener = createChangeListener {
            Assert.assertEquals(true, it)
        }
        settingsViewModel.showChangeLanguageSuccessDialogProperty.addListener(
            showChangeLanguageSuccessDialogListener
        )

        val localeLanguage = mock<LocaleLanguage> {
            on { setPreferredLanguage(any()) } doReturn Completable.complete()
        }
        settingsViewModel.localeLanguage = localeLanguage

        settingsViewModel.updateLanguage(spanish)
    }
}
