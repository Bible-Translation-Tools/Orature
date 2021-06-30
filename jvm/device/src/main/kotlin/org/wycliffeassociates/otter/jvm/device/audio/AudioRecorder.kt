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

import org.slf4j.LoggerFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine
import org.wycliffeassociates.otter.common.device.IAudioRecorder

class AudioRecorder : IAudioRecorder {

    private val logger = LoggerFactory.getLogger(AudioRecorder::class.java)

    override fun pause() {
        line.stop()
        line.close()
    }

    companion object {
        val SAMPLE_RATE = 44100F // Hz
        val SAMPLE_SIZE = 16 // bits
        val CHANNELS = 1
        val SIGNED = true
        val BIG_ENDIAN = false
        val FORMAT = AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        )
        val BUFFER_SIZE = 1024
    }

    private var line: TargetDataLine
    private val audioByteObservable = PublishSubject.create<ByteArray>()

    init {
        line = AudioSystem.getTargetDataLine(FORMAT)
    }

    override fun start() {
        line.open(FORMAT)
        line.start()
        Observable
            .fromCallable {
                val byteArray = ByteArray(BUFFER_SIZE)
                var totalRead = 0
                while (line.isOpen) {
                    totalRead += line.read(byteArray, 0, byteArray.size)
                    audioByteObservable.onNext(byteArray)
                }
            }
            .subscribeOn(Schedulers.io())
            .doOnError { e ->
                logger.error("Error while recording audio", e)
            }
            .subscribe()
    }

    override fun stop() {
        line.stop()
        line.close()
    }

    override fun getAudioStream(): Observable<ByteArray> {
        return audioByteObservable
    }
}
