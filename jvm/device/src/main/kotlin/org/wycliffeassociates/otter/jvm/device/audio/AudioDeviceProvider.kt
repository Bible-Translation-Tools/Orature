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

    fun selectInputDevice(device: Mixer.Info) {
        activeInputDevice as PublishSubject
        activeInputDevice.onNext(device)
    }

    fun selectOutputDevice(device: Mixer.Info) {
        activeOutputDevice as PublishSubject
        activeOutputDevice.onNext(device)
    }

    fun getOutputDeviceNames(): Single<List<String>> {
        return getOutputDevices().map { it.map { it.name } }
    }

    fun getOutputDevices(): Single<List<Mixer.Info>> {
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

    fun getInputDevices(): Single<List<Mixer.Info>> {
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
