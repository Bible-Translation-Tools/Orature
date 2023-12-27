package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import java.io.File

object NarrationAudioBouncerTaskRunner {

    private val logger = LoggerFactory.getLogger(NarrationAudioBouncerTaskRunner::class.java)

    private val audioBouncer = AudioBouncer()
    private var currentTask: Disposable? = null
    private lateinit var emitter: CompletableEmitter

    private fun cancelPreviousTask() {
        if (currentTask != null) {
            audioBouncer.interrupt()
            emitter.onComplete()
        }
    }

    private fun bounceAudioTask(file: File, reader: AudioFileReader, markers: List<AudioMarker>): Completable {
        return Completable
            .create { emitter ->
                audioBouncer.bounceAudio(file, reader, markers)
                this.emitter = emitter
            }
            .doOnComplete {
                logger.info("Successfully completed!")
            }
            .subscribeOn(Schedulers.io())
    }

    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        cancelPreviousTask()
        currentTask = bounceAudioTask(file, reader, markers).subscribe()
    }

}