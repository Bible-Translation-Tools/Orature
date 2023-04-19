/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE

private const val DEFAULT_BUFFER_SIZE = 1024

class ActiveRecordingRenderer(
    stream: Observable<ByteArray>,
    recordingStatus: Observable<Boolean>,
    width: Int,
    secondsOnScreen: Int,
    private val continuous: Boolean
) {
    private val logger = LoggerFactory.getLogger(ActiveRecordingRenderer::class.java)

    private var isActive = AtomicBoolean(false)
    private var recordingActive: Observable<Boolean> = recordingStatus

    // double the width as for each pixel there will be a min and max value
    val floatBuffer = FloatRingBuffer(width * 2)
    val audioData = ArrayList<Float>()

    private val dataReceiver = PublishSubject.create<Float>()

    private val pcmCompressor = PCMCompressor(samplesToCompress(width, secondsOnScreen), dataReceiver)
    val bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    private val compositeDisposable = CompositeDisposable()

    init {
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe { isActive.set(it) }
            .also(compositeDisposable::add)

        bb.order(ByteOrder.LITTLE_ENDIAN)
    }

    val activeRenderer = stream
        .subscribeOn(Schedulers.io())
        .doOnError { e ->
            logger.error("Error in active renderer stream", e)
        }
        .subscribe {
            bb.put(it)
            bb.position(0)
            while (bb.hasRemaining()) {
                val short = bb.short
                if (isActive.get()) {
                    pcmCompressor.add(short.toFloat())
                }
            }
            bb.clear()
        }

    private val dataReceiverDisposable = dataReceiver
        .doOnError { e ->
            logger.error("Error in data receiver stream", e)
        }
        .subscribe {
            if (continuous) {
                audioData.add(it)
            } else {
                floatBuffer.add(it)
            }
        }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        // TODO: get sampleRate from wav file, don't assume 44.1khz
        return (DEFAULT_SAMPLE_RATE * secondsOnScreen) / width
    }

    /** Sets a new status listener and removes the old one */
    fun setRecordingStatusObservable(value: Observable<Boolean>) {
        compositeDisposable.clear()

        recordingActive = value
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe { isActive.set(it) }
            .also(compositeDisposable::add)
    }

    /** Clears rendered data from buffer */
    fun clearData() {
        floatBuffer.clear()
        audioData.clear()
    }

    fun removeListeners() {
        compositeDisposable.clear()
        activeRenderer.dispose()
        dataReceiverDisposable.dispose()
    }
}
