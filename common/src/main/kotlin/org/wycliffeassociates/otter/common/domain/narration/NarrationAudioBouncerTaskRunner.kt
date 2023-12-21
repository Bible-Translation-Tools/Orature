package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import java.io.File
import java.nio.channels.ClosedByInterruptException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object NarrationAudioBouncerTaskRunner {

    private val audioBouncer = AudioBouncer()
    private var currentTask: Disposable? = null

    private fun bounceAudioTask(file: File, reader: AudioFileReader, markers: List<AudioMarker>): Completable {
        return Completable.fromAction {
            audioBouncer.bounceAudio(file, reader, markers)
        }
            .doOnDispose {
                audioBouncer.interrupt()
                println("Cancelled!")
            }
            .doOnComplete {
                println("Successfully completed!")
            }
            .subscribeOn(Schedulers.io())
    }

    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        currentTask?.dispose()
        currentTask = bounceAudioTask(file, reader, markers).subscribe()
    }

}