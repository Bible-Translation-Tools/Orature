package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.device.IAudioDevice
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

val SAMPLE_RATE = 44100F // Hz
val SAMPLE_SIZE = 16 // bits
val CHANNELS = 1
val SIGNED = true
val BIG_ENDIAN = false

class AudioDevice : IAudioDevice {
    private val audioFormat = AudioFormat(
        SAMPLE_RATE,
        SAMPLE_SIZE,
        CHANNELS,
        SIGNED,
        BIG_ENDIAN
    )

    override fun getOutputDevices(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
                val lines = mixer.getSourceLineInfo(info)
                lines.isNotEmpty()
            }.toList()
        }
    }

    override fun getInputDevices(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
                val lines = mixer.getTargetLineInfo(info)
                lines.isNotEmpty()
            }.toList()
        }
    }

    override fun getOutputDevice(name: String?): Maybe<Mixer.Info> {
        return getOutputDevices()
            .flatMapMaybe {
                val device = it.singleOrNull { it.name == name }
                Maybe.just(device)
            }
    }

    override fun getInputDevice(name: String?): Maybe<Mixer.Info> {
        return getInputDevices()
            .flatMapMaybe {
                val device = it.singleOrNull { it.name == name }
                Maybe.just(device)
            }
    }
}
