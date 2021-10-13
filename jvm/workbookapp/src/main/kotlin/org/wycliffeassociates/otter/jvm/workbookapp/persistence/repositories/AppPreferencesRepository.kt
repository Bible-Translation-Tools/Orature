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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider

class AppPreferencesRepository @Inject constructor(
    private val preferences: IAppPreferences,
    private val audioDeviceProvider: AudioDeviceProvider,
    private val languageRepository: ILanguageRepository
) : IAppPreferencesRepository {

    override fun resumeProjectId(): Single<Int> {
        return preferences.resumeBookId()
    }

    override fun setResumeProjectId(id: Int): Completable {
        return preferences.setResumeBookId(id)
    }

    override fun lastResource(): Single<String> {
        return preferences.lastResource()
    }

    override fun setLastResource(resource: String): Completable {
        return preferences.setLastResource(resource)
    }


    override fun getInputDevice(): Single<String> {
        return preferences
            .audioInputDevice()
            .map {
                audioDeviceProvider.getInputDevice(it)?.name ?: ""
            }
    }

    override fun setInputDevice(mixer: String): Completable {
        audioDeviceProvider.selectInputDevice(mixer)
        return preferences.setAudioInputDevice(mixer)
    }

    override fun getOutputDevice(): Single<String> {
        return preferences
            .audioOutputDevice()
            .map {
                audioDeviceProvider.getOutputDevice(it)?.name ?: ""
            }
    }

    override fun setOutputDevice(mixer: String): Completable {
        audioDeviceProvider.selectOutputDevice(mixer)
        return preferences.setAudioOutputDevice(mixer)
    }

    override fun localeLanguage(): Maybe<Language> {
        return preferences
            .localeLanguage()
            .flatMapMaybe {
                if (it.isNotEmpty()) {
                    languageRepository
                        .getBySlug(it)
                        .toMaybe()
                } else {
                    null
                }
            }
    }

    override fun setLocaleLanguage(language: Language): Completable {
        return preferences.setLocaleLanguage(language.slug)
    }
}
