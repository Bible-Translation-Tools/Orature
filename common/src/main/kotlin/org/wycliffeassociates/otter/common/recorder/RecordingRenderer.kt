package org.wycliffeassociates.otter.common.recorder

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

private const val DEFAULT_BUFFER_SIZE = 1024

abstract class RecordingRenderer(
    stream: Observable<ByteArray>,
    recordingStatus: Observable<Boolean>,
    width: Int,
    secondsOnScreen: Int
) {
    val logger: Logger = LoggerFactory.getLogger(RecordingRenderer::class.java)

    private val isActive = AtomicBoolean(false)
    private var recordingActive = recordingStatus

    val dataReceiver = PublishSubject.create<Float>()
    private val pcmCompressor = PCMCompressor(samplesToCompress(width, secondsOnScreen), dataReceiver)
    val bb: ByteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    private val compositeDisposable = CompositeDisposable()

    abstract val dataReceiverDisposable: Disposable

    private val activeRenderer = stream
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

    init {
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe {
                isActive.set(it)
            }
            .also(compositeDisposable::add)

        bb.order(ByteOrder.LITTLE_ENDIAN)
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
    abstract fun clearData()

    fun removeListeners() {
        compositeDisposable.clear()
        activeRenderer.dispose()
        dataReceiverDisposable.dispose()
    }
}