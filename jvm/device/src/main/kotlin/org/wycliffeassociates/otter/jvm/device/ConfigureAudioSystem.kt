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
package org.wycliffeassociates.otter.jvm.device

import io.reactivex.Maybe
import javax.inject.Inject
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.device.audio.DEFAULT_AUDIO_FORMAT

class ConfigureAudioSystem @Inject constructor(
    private val connectionFactory: AudioConnectionFactory,
    private val deviceProvider: AudioDeviceProvider,
    private val preferencesRepository: IAppPreferencesRepository
) {

    private val logger = LoggerFactory.getLogger(ConfigureAudioSystem::class.java)

    fun configure() {
        configureLines()
        configureListener()
    }

    private fun configureLines() {
        val outputLine = getOutputLine()
        connectionFactory.setOutputLine(outputLine)

        val inputLine = getInputLine()
        connectionFactory.setInputLine(inputLine)
    }

    private fun configureListener() {
        deviceProvider.activeOutputDevice.subscribe { mixer ->
            var newLine: SourceDataLine? = null
            try {
                newLine = AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, mixer)
            } catch (e: Exception) {
                logger.error("Error in changing active output device.", e)
            }
            if (newLine != null) {
                connectionFactory.setOutputLine(newLine)
            }
        }

        deviceProvider.activeInputDevice.subscribe { mixer ->
            var newLine: TargetDataLine? = null
            try {
                newLine = AudioSystem.getTargetDataLine(DEFAULT_AUDIO_FORMAT, mixer)
            } catch (e: Exception) {
                logger.error("Error in changing active input device.", e)
            }
            if (newLine != null) {
                connectionFactory.setInputLine(newLine)
            }
        }
    }

    private fun getOutputLine(): SourceDataLine? {
        return preferencesRepository
            .getOutputDevice()
            .map { deviceName ->
                val names = deviceProvider.getOutputDeviceNames()
                if (deviceName.isBlank() && names.isNotEmpty()) names.first() else deviceName
            }
            .map { deviceName ->
                preferencesRepository.setOutputDevice(deviceName).blockingGet()
                deviceName
            }
            .map { deviceName ->
                val mixer = deviceProvider.getOutputDevice(deviceName)
                var line: SourceDataLine? = null
                mixer?.let {
                    try {
                        line = AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, mixer)
                    } catch (e: Exception) {
                    }
                }
                if (line != null) Maybe.just(line) else Maybe.empty()
            }.flatMapMaybe { it }.blockingGet()
    }

    private fun getInputLine(): TargetDataLine? {
        return preferencesRepository
            .getInputDevice()
            .map { deviceName ->
                val names = deviceProvider.getInputDeviceNames()
                if (deviceName.isBlank() && names.isNotEmpty()) names.first() else deviceName
            }
            .map { deviceName ->
                preferencesRepository.setInputDevice(deviceName).blockingGet()
                deviceName
            }
            .map { deviceName ->
                val mixer = deviceProvider.getInputDevice(deviceName)
                var line: TargetDataLine? = null
                mixer?.let {
                    try {
                        line = AudioSystem.getTargetDataLine(DEFAULT_AUDIO_FORMAT, mixer)
                    } catch (e: Exception) {
                    }
                }
                if (line != null) Maybe.just(line) else Maybe.empty()
            }.flatMapMaybe { it }.blockingGet()
    }
}
