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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.ILocaleLanguage
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject
import javax.sound.sampled.Mixer

class SettingsViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(SettingsViewModel::class.java)

    @Inject
    lateinit var audioDeviceProvider: AudioDeviceProvider

    @Inject
    lateinit var appPrefRepo: IAppPreferencesRepository

    @Inject
    lateinit var pluginRepository: IAudioPluginRepository

    @Inject
    lateinit var localeLanguage: ILocaleLanguage

    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val audioPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()

    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedMarkerProperty = SimpleObjectProperty<AudioPluginData>()

    val supportedLocaleLanguages = observableListOf<Language>()
    val selectedLocaleLanguageProperty = SimpleObjectProperty<Language>()

    val showChangeLanguageSuccessDialogProperty = SimpleBooleanProperty(false)

    val outputDevices = observableListOf<Mixer.Info>()
    val selectedOutputDeviceProperty = SimpleObjectProperty<Mixer.Info>()

    val inputDevices = observableListOf<Mixer.Info>()
    val selectedInputDeviceProperty = SimpleObjectProperty<Mixer.Info>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        audioPluginViewModel.selectedEditorProperty.bind(selectedEditorProperty)
        audioPluginViewModel.selectedRecorderProperty.bind(selectedRecorderProperty)
        audioPluginViewModel.selectedMarkerProperty.bind(selectedMarkerProperty)

        loadOutputDevices()
        loadInputDevices()
        loadCurrentOutputDevice()
        loadCurrentInputDevice()

        supportedLocaleLanguages.setAll(localeLanguage.supportedLanguages)
        selectedLocaleLanguageProperty.set(localeLanguage.actualLanguage)
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
        appPrefRepo.getOutputDevice()
            .doOnError {
                logger.error("Error in loadCurrentOutputDevice: ", it)
            }
            .subscribe { device ->
                selectedOutputDeviceProperty.set(device)
            }
    }

    private fun loadCurrentInputDevice() {
        appPrefRepo.getInputDevice()
            .doOnError {
                logger.error("Error in loadCurrentInputDevice: ", it)
            }
            .subscribe { device ->
                selectedInputDeviceProperty.set(device)
            }
    }

    private fun loadOutputDevices() {
        audioDeviceProvider.getOutputDevices()
            .doOnError {
                logger.error("Error in loadOutputDevices: ", it)
            }
            .subscribe { players ->
                outputDevices.setAll(players)
            }
    }

    private fun loadInputDevices() {
        audioDeviceProvider.getInputDevices()
            .doOnError {
                logger.error("Error in loadInputDevices: ", it)
            }
            .subscribe { recorders ->
                inputDevices.setAll(recorders)
            }
    }

    fun updateOutputDevice(mixer: Mixer.Info) {
        appPrefRepo.setOutputDevice(mixer).subscribe()
    }

    fun updateInputDevice(mixer: Mixer.Info) {
        appPrefRepo.setInputDevice(mixer).subscribe()
    }

    fun updateLanguage(language: Language) {
        appPrefRepo
            .setLocaleLanguage(language)
            .subscribe {
                showChangeLanguageSuccessDialogProperty.set(true)
            }
    }
}
