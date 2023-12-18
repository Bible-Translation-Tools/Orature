package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object NarrationAudioBouncerTaskRunner {
    private val queue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private var pool = ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, queue)

    val busy = PublishSubject.create<Boolean>()

    private val audioBouncer = AudioBouncer()

    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        audioBouncer.interrupt()

        queue.clear()
        queue.add {
            busy.onNext(true)
            audioBouncer.bounceAudio(file, reader, markers)
            busy.onNext(false)
        }
    }

    fun close() {
        pool.shutdown()
    }

    fun start() {
        pool = ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, queue)
        pool.prestartCoreThread()
    }
}