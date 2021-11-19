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

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalArgumentException
import javax.sound.sampled.LineUnavailableException
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import javax.sound.sampled.TargetDataLine

private const val DEFAULT_BUFFER_SIZE = 1024

class AudioRecorder(
    val line: TargetDataLine?,
    private val errorRelay: PublishRelay<AudioError> = PublishRelay.create()
) : IAudioRecorder {

    private val monitor = Object()

    @Volatile
    private var stop = false

    @Volatile
    private var pause = false

    private val audioByteObservable = PublishSubject.create<ByteArray>()
    private val recordingStream = Observable
        .fromCallable {
            val byteArray = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalRead = 0
            line?.let {
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
            }
            stop = false
        }
        .subscribeOn(Schedulers.io())
        .subscribe()

    @Synchronized // Synchronized so as to not subscribe to multiple streams on quick multipress
    override fun start() {
        pause = false
        try {
            line?.open()
            line?.start()
        } catch (e: LineUnavailableException) {
            errorRelay.accept(AudioError(AudioErrorType.RECORDING, e))
        } catch (e: IllegalArgumentException) {
            errorRelay.accept(AudioError(AudioErrorType.RECORDING, e))
        }
        synchronized(monitor) {
            monitor.notify()
        }
    }

    override fun pause() {
        line?.stop()
        pause = true
    }

    override fun stop() {
        line?.stop()
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
