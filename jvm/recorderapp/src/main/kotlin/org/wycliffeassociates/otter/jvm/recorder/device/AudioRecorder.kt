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
package org.wycliffeassociates.otter.jvm.recorder.device

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class AudioRecorder : IAudioRecorder {

    private val monitor = Object()

    @Volatile
    private var stop = false
    @Volatile
    private var pause = false

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

    private var line = AudioSystem.getTargetDataLine(FORMAT)
    private val audioByteObservable = PublishSubject.create<ByteArray>()
    private val recordingStream = Observable
        .fromCallable {
            val byteArray = ByteArray(BUFFER_SIZE)
            var totalRead = 0
            while (true) {
                if (line.isOpen || line.available() > 0) {
                    totalRead += line.read(byteArray, 0, byteArray.size)
                    audioByteObservable.onNext(byteArray)
                } else {
                    try {
                        synchronized(monitor) {
                            monitor.wait()
                        }
                    } catch (e: InterruptedException) {
                        stop()
                    }
                }
                if (stop || pause) {
                    line.close()
                    if (stop) break
                }
            }
            stop = false
        }
        .subscribeOn(Schedulers.io())
        .subscribe()

    @Synchronized // Synchronized so as to not subscribe to multiple streams on quick multipress
    override fun start() {
        pause = false
        line.open(FORMAT)
        line.start()
        synchronized(monitor) {
            monitor.notify()
        }
    }

    override fun pause() {
        line.stop()
        pause = true
    }

    override fun stop() {
        line.stop()
        stop = true

        // wakes up the recording thread to allow it to close
        synchronized(monitor) {
            monitor.notify()
        }
        audioByteObservable.onComplete()
    }

    override fun getAudioStream(): Observable<ByteArray> {
        return audioByteObservable
    }
}
