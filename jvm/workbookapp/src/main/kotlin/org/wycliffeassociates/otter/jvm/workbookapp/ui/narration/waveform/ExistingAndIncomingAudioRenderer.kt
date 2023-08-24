package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean


class ExistingAndIncomingAudioRenderer(
    val existingAudioReader : AudioFileReader,
    val incomingAudioStream : Observable<ByteArray>,
    recordingStatus: Observable<Boolean>,
    val width: Int,
    secondsOnScreen: Int) {

    private val DEFAULT_BUFFER_SIZE = 1024
    private val logger = LoggerFactory.getLogger(ActiveRecordingRenderer::class.java)

    private var isActive = AtomicBoolean(false)
    private var recordingActive: Observable<Boolean> = recordingStatus

    // double the width as for each pixel there will be a min and max value
    val floatBuffer = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(floatBuffer, samplesToCompress(width, secondsOnScreen))
    val bb = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    val compositeDisposable = CompositeDisposable()

    val existingAudioHolder = ByteArray(DEFAULT_SAMPLE_RATE * secondsOnScreen * 2)

    init {
        recordingActive
            .doOnError { e ->
                logger.error("Error in active recording listener", e)
            }
            .subscribe { isActive.set(it) }
            .also {
                compositeDisposable.add(it)
            }
        bb.order(ByteOrder.LITTLE_ENDIAN)
    }

    // NOTE: I would like to subscribe to something (possibly the position of the play-head) that will call
    // existingAudioReader.getPCMBuffer / fillExistingAudioHolder

    val activeRenderer = incomingAudioStream
        .subscribeOn(Schedulers.io())
        .doOnError { e ->
            logger.error("Error in active renderer stream", e)
        }
        .subscribe {
            if(isActive.get()) {
                bb.put(it)
                bb.position(0)

                if(floatBuffer.size() == 0) { // NOTE: for this to work, the floatBuffer MUST be cleared when switched to recording mode
                    // fill with offset + existingAudio
                    // TODO: figure out why this is never being called?
                    println("filling with existing 1")
                    fillExistingAudioHolder()
                }

                while (bb.hasRemaining()) {
                    val short = bb.short
                    if (isActive.get()) {
                        //println("accumulating accumulate totelIncomingAudioBytes variable/property")
                        pcmCompressor.add(short.toFloat())
                        // accumulate totelIncomingAudioBytes variable/property
                    }
                }
                bb.clear()
            }

        }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        // TODO: get samplerate from wav file, don't assume 44.1khz
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
            .also {
                compositeDisposable.add(it)
            }
    }

    /** Clears rendered data from buffer */
    fun clearData() {
        floatBuffer.clear()
    }

    fun fillExistingAudioHolder(): Int {
        val bytesFromExisting = existingAudioReader.getPcmBuffer(this.existingAudioHolder)
        val offset = existingAudioHolder.size - bytesFromExisting

        var i = 0
        while( i < offset) {
            pcmCompressor.add(0.0F)
            i++
        }

        i = 0
        while(i < bytesFromExisting - 1) {
            val short = ((existingAudioHolder[i + 1].toInt() shl 8) or (existingAudioHolder[i].toInt() and 0xFF)).toShort()
            pcmCompressor.add(short.toFloat())
            i+=2
        }

        return bytesFromExisting
    }
}
