package org.wycliffeassociates.otter.jvm.device

import javax.inject.Inject
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.device.audio.DEFAULT_AUDIO_FORMAT

class ConfigureAudioSystem @Inject constructor(
    private val connectionFactory: AudioConnectionFactory,
    private val deviceProvider: AudioDeviceProvider,
    private val preferencesRepository: IAppPreferencesRepository
) {
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
            val newLine = AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, mixer)
            connectionFactory.setOutputLine(newLine)
        }

        deviceProvider.activeInputDevice.subscribe { mixer ->
            val newLine = AudioSystem.getTargetDataLine(DEFAULT_AUDIO_FORMAT, mixer)
            connectionFactory.setInputLine(newLine)
        }
    }

    private fun getOutputLine(): SourceDataLine {
        return preferencesRepository
            .getOutputDevice()
            .map {
                if(it.isBlank()) deviceProvider.getOutputDeviceNames().first() else it
            }
            .map {
                preferencesRepository.setOutputDevice(it).blockingGet()
                it
            }
            .map { deviceProvider.getOutputDevice(it) }
            .map { AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, it) }
            .blockingGet()
    }

    private fun getInputLine(): TargetDataLine {
        return preferencesRepository
            .getInputDevice()
            .map {
                if(it.isBlank()) deviceProvider.getInputDeviceNames().first() else it
            }
            .map {
                preferencesRepository.setInputDevice(it).blockingGet()
                it
            }
            .map { deviceProvider.getInputDevice(it) }
            .map { AudioSystem.getTargetDataLine(DEFAULT_AUDIO_FORMAT, it) }
            .blockingGet()
    }

    private fun getDefaultAudioDevice(): Mixer.Info {
        return AudioSystem.getMixerInfo().first()
    }

    fun checkIfDeviceIsAvailable(device: String): Boolean {
        return deviceProvider.getOutputDevice(device) != null
    }
}
