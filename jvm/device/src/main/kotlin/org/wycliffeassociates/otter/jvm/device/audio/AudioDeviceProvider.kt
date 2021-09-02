package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

class AudioDeviceProvider(private val audioFormat: AudioFormat) {

    val activeInputDevice: Observable<Mixer.Info> = PublishSubject.create()
    val activeOutputDevice: Observable<Mixer.Info> = PublishSubject.create()

    fun selectInputDevice(deviceName: String) {
        val device = getInputDevice(deviceName)
        device.subscribe {
            activeInputDevice as PublishSubject
            activeInputDevice.onNext(it)
        }
    }

    fun selectOutputDevice(deviceName: String) {
        val device = getOutputDevice(deviceName)
        device.subscribe {
            activeOutputDevice as PublishSubject
            activeOutputDevice.onNext(it)
        }
    }

    fun getOutputDeviceNames(): Single<List<String>> {
        return getOutputDevices().map { it.map { it.name } }
    }

    private fun getOutputDevices(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
                val lines = mixer.getSourceLineInfo(info)
                lines.isNotEmpty()
            }.toList().map { it }
        }
    }

    private fun getInputDevices(): Single<List<Mixer.Info>> {
        return Single.fromCallable {
            val mixers = AudioSystem.getMixerInfo()
            mixers.filter { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
                val lines = mixer.getTargetLineInfo(info)
                lines.isNotEmpty()
            }.toList().map { it }
        }
    }

    fun getInputDeviceNames(): Single<List<String>> {
        return getInputDevices().map { it.map { it.name } }
    }

    fun getOutputDevice(name: String?): Maybe<Mixer.Info> {
        return getOutputDevices()
            .flatMapMaybe {
                var device = it.singleOrNull { it.name == name }
                device?.let { Maybe.just(device) } ?: Maybe.empty()
            }
    }

    fun getInputDevice(name: String?): Maybe<Mixer.Info> {
        return getOutputDevices()
            .flatMapMaybe {
                val device = it.singleOrNull { it.name == name }
                device?.let { Maybe.just(device) } ?: Maybe.empty()
            }
    }
}
