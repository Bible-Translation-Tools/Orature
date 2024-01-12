package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File


enum class TaskRunnerStatus {
    BOUNCING_AUDIO,
    UPDATING_MARKERS,
    IDLE
}


object NarrationTakeModifierTaskRunner {

    private val logger = LoggerFactory.getLogger(NarrationTakeModifierTaskRunner::class.java)

    private val audioBouncer = AudioBouncer()

    private var currentAudioBounceTask: Disposable? = null
    private lateinit var currentAudioBounceTaskEmitter: CompletableEmitter

    private var currentMarkerUpdateTask: Disposable? = null
    private var currentUpdateMarkerTaskEmitter: CompletableEmitter? = null
    private var waitingMarkerUpdateTask: Disposable? = null

    private var busyStatusEmitter: ObservableEmitter<TaskRunnerStatus>? = null
    val busyStatus: Observable<TaskRunnerStatus> = Observable
        .create {
            busyStatusEmitter = it
        }
    private var isBouncingAudio = false


    @Synchronized
    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        updateIsBouncingAudio(true)

        waitingMarkerUpdateTask?.dispose()
        currentAudioBounceTask?.dispose()

        cancelPreviousAudioBounceTask()
        currentAudioBounceTask = bounceAudioTask(file, reader, markers).subscribe()
    }


    private fun updateIsBouncingAudio(newValue: Boolean) {
        isBouncingAudio = newValue
        if (newValue) {
            busyStatusEmitter?.onNext(TaskRunnerStatus.BOUNCING_AUDIO)

        } else {
            busyStatusEmitter?.onNext(TaskRunnerStatus.IDLE)
        }
    }


    private fun cancelPreviousAudioBounceTask() {
        if (currentAudioBounceTask != null) {
            audioBouncer.interrupt()
            currentMarkerUpdateTask?.dispose()
        }
    }

    private fun bounceAudioTask(
        file: File,
        reader: AudioFileReader,
        markers: List<AudioMarker>
    ): Completable {

        return Completable
            .create { emitter ->
                this.currentAudioBounceTaskEmitter = emitter
                audioBouncer.bounceAudio(file, reader, markers)
                emitter.onComplete()
            }
            .doOnDispose {
                logger.info("Canceling audio bounce task")
            }
            .doOnComplete {
                synchronized(this) {
                    updateIsBouncingAudio(false)
                }
            }
            .subscribeOn(Schedulers.io())
    }


    @Synchronized
    fun updateMarkers(file: File, markers: List<AudioMarker>) {

        if (!isBouncingAudio) {
            currentMarkerUpdateTask?.dispose()
            currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()

        } else {

            busyStatusEmitter?.onNext(TaskRunnerStatus.UPDATING_MARKERS)

            busyStatus
                .takeWhile { busy ->
                    busy == TaskRunnerStatus.UPDATING_MARKERS || busy == TaskRunnerStatus.BOUNCING_AUDIO
                }
                .doOnComplete {
                    currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()
                }
                .doOnDispose {
                    logger.info("Disposing unnecessary marker update")
                }
                .doOnSubscribe {
                    currentMarkerUpdateTask?.dispose()
                }
                .subscribe().let {
                    waitingMarkerUpdateTask?.dispose()
                    waitingMarkerUpdateTask = it
                }

        }

    }


    private fun updateMarkersTask(file: File, markers: List<AudioMarker>): Completable {
        return Completable
            .create { emitter ->
                busyStatusEmitter?.onNext(TaskRunnerStatus.UPDATING_MARKERS)
                logger.info("Started updating markers")
                this.currentUpdateMarkerTaskEmitter = emitter

                val oaf = OratureAudioFile(file)
                clearNarrationMarkers(oaf)
                addNarrationMarkers(oaf, markers)
                oaf.update()

                emitter.onComplete()
            }
            .doFinally {
                logger.info("Finished updating markers")
                busyStatusEmitter?.onNext(TaskRunnerStatus.IDLE)
            }
            .subscribeOn(Schedulers.io())
    }


    private fun clearNarrationMarkers(audioFile: OratureAudioFile) {
        audioFile.clearMarkersOfType<VerseMarker>()
        audioFile.clearMarkersOfType<ChapterMarker>()
        audioFile.clearMarkersOfType<BookMarker>()
    }

    private fun addNarrationMarkers(audioFile: OratureAudioFile, markers: List<AudioMarker>) {
        markers.forEach { marker ->
            audioFile.addMarker(audioFile.getMarkerTypeFromClass(marker::class), marker)
        }
    }

}