package org.wycliffeassociates.otter.jvm.device

import javax.inject.Inject
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
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
        println("configuring audio system")
        configureLines()
        configureListener()
    }

    private fun configureLines() {
        val outputLine = getOutputLine()
        connectionFactory.setOutputLine(outputLine)
    }

    private fun configureListener() {
        deviceProvider.activeOutputDevice.subscribe { mixer ->
            val newLine = AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, mixer)
            connectionFactory.setOutputLine(newLine)
        }
    }

    private fun getOutputLine(): SourceDataLine {
        return preferencesRepository
            .getOutputDevice()
            .map {
                println("Preference output line is $it")
                if(it.isBlank()) deviceProvider.getOutputDeviceNames().first() else it
            }
            .map {
                println("Initialized output line is $it")
                preferencesRepository.setOutputDevice(it).blockingGet()
                it
            }
            .map { deviceProvider.getOutputDevice(it) }
            .map { AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT, it) }
            .blockingGet()
    }

    private fun getDefaultAudioDevice(): Mixer.Info {
        return AudioSystem.getMixerInfo().first()
    }

    fun checkIfDeviceIsAvailable(device: String): Boolean {
        return deviceProvider.getOutputDevice(device) != null
    }
}
