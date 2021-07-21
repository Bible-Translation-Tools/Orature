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
import org.wycliffeassociates.otter.common.device.IAudioDevice
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import javax.inject.Inject
import javax.sound.sampled.Mixer

class AppPreferencesRepository @Inject constructor(
    private val preferences: IAppPreferences,
    private val audioDevice: IAudioDevice
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

    override fun getInputDevice(): Maybe<Mixer.Info> {
        return preferences.audioInputDevice()
            .flatMapMaybe {
                audioDevice.getOutputDevice(it)
            }
    }

    override fun setOutputDevice(mixer: Mixer.Info): Completable {
        return preferences.setAudioOutputDevice(mixer.name)
    }

    override fun getOutputDevice(): Maybe<Mixer.Info> {
        return preferences.audioOutputDevice()
            .flatMapMaybe {
                audioDevice.getInputDevice(it)
            }
    }

    override fun setInputDevice(mixer: Mixer.Info): Completable {
        return preferences.setAudioInputDevice(mixer.name)
    }
}
