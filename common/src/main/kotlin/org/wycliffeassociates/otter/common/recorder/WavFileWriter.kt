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
