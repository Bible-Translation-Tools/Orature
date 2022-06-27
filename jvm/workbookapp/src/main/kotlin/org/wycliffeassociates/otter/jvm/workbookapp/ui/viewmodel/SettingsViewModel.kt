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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jthemedetecor.OsThemeDetector
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.NodeOrientation
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.theme.AppTheme
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.ThemeColorEvent
import tornadofx.*
import javax.inject.Inject

class SettingsViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(SettingsViewModel::class.java)

    @Inject lateinit var audioDeviceProvider: AudioDeviceProvider
    @Inject lateinit var appPrefRepository: IAppPreferencesRepository
    @Inject lateinit var pluginRepository: IAudioPluginRepository
    @Inject lateinit var localeLanguage: LocaleLanguage
    @Inject lateinit var theme: AppTheme
    @Inject lateinit var importLanguages: ImportLanguages

    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    val audioPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()

    val supportedThemes = observableListOf<ColorTheme>()
    val selectedThemeProperty = SimpleObjectProperty<ColorTheme>()

    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedMarkerProperty = SimpleObjectProperty<AudioPluginData>()

    val supportedLocaleLanguages = observableListOf<Language>()
    val selectedLocaleLanguageProperty = SimpleObjectProperty<Language>()

    val showChangeLanguageSuccessDialogProperty = SimpleBooleanProperty(false)

    val outputDevices = observableListOf<String>()
    val selectedOutputDeviceProperty = SimpleObjectProperty<String>()

    val inputDevices = observableListOf<String>()
    val selectedInputDeviceProperty = SimpleObjectProperty<String>()

    val appColorMode = SimpleObjectProperty<ColorTheme>()
    private val osThemeDetector = OsThemeDetector.getDetector()
    private val isOSDarkMode = SimpleBooleanProperty(osThemeDetector.isDark)

    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val orientationScaleProperty = orientationProperty.doubleBinding {
        when (it) {
            NodeOrientation.RIGHT_TO_LEFT -> -1.0
            else -> 1.0
        }
    }
    val sourceOrientationProperty = workbookDataStore.activeWorkbookProperty.objectBinding {
        when (it?.source?.language?.direction) {
            "rtl" -> NodeOrientation.RIGHT_TO_LEFT
            else -> NodeOrientation.LEFT_TO_RIGHT
        }
    }

    val languageNamesUrlProperty = SimpleStringProperty()
    val defaultLanguageNamesUrlProperty = SimpleStringProperty()
    val languageNamesImportingProperty = SimpleBooleanProperty(false)
    val updateLanguagesResultProperty = SimpleStringProperty()
    val updateLanguagesSuccessProperty = SimpleObjectProperty<Boolean>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        audioPluginViewModel.selectedEditorProperty.bind(selectedEditorProperty)
        audioPluginViewModel.selectedRecorderProperty.bind(selectedRecorderProperty)
        audioPluginViewModel.selectedMarkerProperty.bind(selectedMarkerProperty)

        osThemeDetector.registerListener {
            runLater { isOSDarkMode.set(it) }
        }
        subscribe<ThemeColorEvent<UIComponent>> {
            updateTheme(it.data)
        }
    }

    fun bind() {
        loadOutputDevices()
        loadInputDevices()
        loadCurrentOutputDevice()
        loadCurrentInputDevice()
        loadLanguageNamesUrl()
        loadDefaultLanguageNamesUrl()

        supportedThemes.setAll(ColorTheme.values().asList())
        theme.preferredTheme
            .observeOnFx()
            .subscribe { theme ->
                selectedThemeProperty.set(theme)
            }

        supportedLocaleLanguages.setAll(localeLanguage.supportedLanguages)
        selectedLocaleLanguageProperty.set(localeLanguage.preferredLanguage)
    }

    fun refreshPlugins() {
        pluginRepository
            .getAll()
            .observeOnFx()
            .doOnSuccess { plugins ->
                audioPlugins.setAll(
                    plugins.filter { it.canEdit || it.canRecord }
                )
            }
            .observeOn(Schedulers.io())
            .flatMapMaybe {
                pluginRepository.getPluginData(PluginType.RECORDER)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedRecorderProperty.set(it)
            }
            .observeOn(Schedulers.io())
            .flatMap {
                pluginRepository.getPluginData(PluginType.EDITOR)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedEditorProperty.set(it)
            }
            .observeOn(Schedulers.io())
            .flatMap {
                pluginRepository.getPluginData(PluginType.MARKER)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedMarkerProperty.set(it)
            }
            .doOnError { e -> logger.error("Error in refreshPlugins", e) }
            .subscribe()
    }

    fun selectEditor(editorData: AudioPluginData) {
        pluginRepository.setPluginData(PluginType.EDITOR, editorData).subscribe()
        selectedEditorProperty.set(editorData)
    }

    fun selectRecorder(recorderData: AudioPluginData) {
        pluginRepository.setPluginData(PluginType.RECORDER, recorderData).subscribe()
        selectedRecorderProperty.set(recorderData)
    }

    private fun loadCurrentOutputDevice() {
        appPrefRepository.getOutputDevice()
            .doOnError {
                logger.error("Error in loadCurrentOutputDevice: ", it)
            }
            .observeOnFx()
            .subscribe { device ->
                selectedOutputDeviceProperty.set(device)
            }
    }

    private fun loadCurrentInputDevice() {
        appPrefRepository.getInputDevice()
            .doOnError {
                logger.error("Error in loadCurrentInputDevice: ", it)
            }
            .observeOnFx()
            .subscribe { device ->
                selectedInputDeviceProperty.set(device)
            }
    }

    private fun loadLanguageNamesUrl() {
        appPrefRepository.languageNamesUrl()
            .doOnError {
                logger.error("Error in loadLanguageNamesUrl: ", it)
            }
            .observeOnFx()
            .subscribe { url ->
                languageNamesUrlProperty.set(url)
            }
    }

    private fun loadDefaultLanguageNamesUrl() {
        appPrefRepository.defaultLanguageNamesUrl()
            .doOnError {
                logger.error("Error in loadDefaultLanguageNamesUrl: ", it)
            }
            .observeOnFx()
            .subscribe { url ->
                defaultLanguageNamesUrlProperty.set(url)
            }
    }

    private fun loadOutputDevices() {
        val devices = audioDeviceProvider.getOutputDeviceNames()
        outputDevices.setAll(devices)
    }

    private fun loadInputDevices() {
        val devices = audioDeviceProvider.getInputDeviceNames()
        inputDevices.setAll(devices)
    }

    fun updateOutputDevice(mixer: String) {
        appPrefRepository.setOutputDevice(mixer).subscribe()
    }

    fun updateInputDevice(mixer: String) {
        appPrefRepository.setInputDevice(mixer).subscribe()
    }

    fun refreshDevices() {
        loadOutputDevices()
        loadInputDevices()
        loadCurrentOutputDevice()
        loadCurrentInputDevice()
    }

    fun updateLanguage(language: Language) {
        localeLanguage.setPreferredLanguage(language)
            .subscribe {
                showChangeLanguageSuccessDialogProperty.set(true)
            }
    }

    fun setAppOrientation() {
        orientationProperty.set(
            when (localeLanguage.preferredLanguage?.direction) {
                "rtl" -> NodeOrientation.RIGHT_TO_LEFT
                else -> NodeOrientation.LEFT_TO_RIGHT
            }
        )
    }

    fun updateTheme(selectedTheme: ColorTheme) {
        if (selectedTheme == ColorTheme.SYSTEM) {
            bindSystemTheme()
        } else {
            appColorMode.unbind()
            appColorMode.set(selectedTheme)
        }

        theme.setPreferredThem(selectedTheme)
            .subscribe()
    }

    fun updateLanguageNamesUrl() {
        appPrefRepository
            .setLanguageNamesUrl(languageNamesUrlProperty.value)
            .subscribe()
    }

    fun importLanguages() {
        if (languageNamesImportingProperty.value) return

        languageNamesImportingProperty.set(true)

        fetchLanguageNames()
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { response ->
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.let { stream ->
                        importLanguages.update(stream)
                    }
                } else {
                    val error = Throwable("Response code: ${response.code()}")
                    Completable.error(error)
                }
            }
            .observeOnFx()
            .doOnError {
                logger.error("Error in importLanguages: ", it)
            }
            .subscribe(
                {
                    languageNamesImportingProperty.set(false)
                    updateLanguagesSuccessProperty.set(true)
                    updateLanguagesResultProperty.set(messages["success"])
                },
                {
                    languageNamesImportingProperty.set(false)
                    updateLanguagesSuccessProperty.set(false)
                    updateLanguagesResultProperty.set(it.message)
                }
            )
    }

    fun resetLanguageNamesLocation() {
        appPrefRepository
            .resetLanguageNamesUrl()
            .subscribe { url ->
                languageNamesUrlProperty.set(url)
            }
    }

    private fun fetchLanguageNames(): Single<Response> {
        return Single.fromCallable {
            val request = Request.Builder()
                .url(languageNamesUrlProperty.value)
                .build()

            val httpClient = OkHttpClient()
            httpClient.newCall(request).execute()
        }
    }

    private fun bindSystemTheme() {
        appColorMode.bind(isOSDarkMode.objectBinding {
            if (it == true)
                ColorTheme.DARK
            else
                ColorTheme.LIGHT
        })
    }
}
