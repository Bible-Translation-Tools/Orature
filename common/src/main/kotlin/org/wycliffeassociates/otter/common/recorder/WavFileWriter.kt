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
package org.wycliffeassociates.otter.common.recorder

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import org.wycliffeassociates.otter.common.audio.AudioFile

class WavFileWriter(
    private val audioFile: AudioFile,
    private val audioStream: Observable<ByteArray>,
    private val onComplete: () -> Unit
) {
    private val logger = LoggerFactory.getLogger(WavFileWriter::class.java)

    private var record = AtomicBoolean(false)
    private val writingSubject = PublishSubject.create<Boolean>()
    val isWriting = writingSubject.map { it }

    fun start() {
        record.set(true)
        writingSubject.onNext(true)
    }

    fun pause() {
        record.set(false)
        writingSubject.onNext(false)
    }

    val writer = Observable
        .using(
            {
                audioFile.writer(append = false, buffered = true)
            },
            { writer ->
                audioStream.map {
                    if (record.get()) {
                        writer.write(it)
                    }
                }
            },
            { writer ->
                writer.close()
                writingSubject.onComplete()
                onComplete()
            }
        )
        .subscribeOn(Schedulers.io())
        .doOnError { e -> logger.error("Error in WavFileWriter", e) }
        .subscribe()
}
