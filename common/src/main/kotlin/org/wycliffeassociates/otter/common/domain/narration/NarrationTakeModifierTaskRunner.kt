package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
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
    UPDATE_MARKERS_WAITING,
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

    private var statusEmitter: ObservableEmitter<TaskRunnerStatus>? = null
    val status: ConnectableObservable<TaskRunnerStatus> = Observable
        .create {
            statusEmitter = it
        }.publish()
    private var isBouncingAudio = false

    init {
        status.connect()
    }

    @Synchronized
    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        updateStatus(TaskRunnerStatus.BOUNCING_AUDIO)

        // Prevents waiting marker update task from executing after ongoing audio bounce tasks are canceled.
        cancelMarkerUpdateTask()

        cancelPreviousAudioBounceTask()
        currentAudioBounceTask = bounceAudioTask(file, reader, markers).subscribe()
    }


    private fun updateStatus(status: TaskRunnerStatus) {
        isBouncingAudio =
            (status == TaskRunnerStatus.BOUNCING_AUDIO || status == TaskRunnerStatus.UPDATE_MARKERS_WAITING)
        statusEmitter?.onNext(status)
    }

    private fun cancelMarkerUpdateTask() {
        // Disposes the waiting marker update task before disposing
        waitingMarkerUpdateTask?.dispose()
        waitingMarkerUpdateTask = null

        // Disposes the current marker update task (if any)
        currentMarkerUpdateTask?.dispose()
        currentMarkerUpdateTask = null
    }

    private fun cancelPreviousAudioBounceTask() {
        if (currentAudioBounceTask != null) {
            audioBouncer.interrupt()
            currentAudioBounceTask?.dispose()
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
                    if (waitingMarkerUpdateTask == null) {
                        // Only set status to IDLE when we do not have a waiting update marker task
                        updateStatus(TaskRunnerStatus.IDLE)
                    } else {
                        // Sets status to UPDATING_MARKERS so the waiting update marker task can start executing
                        updateStatus(TaskRunnerStatus.UPDATING_MARKERS)
                    }
                }
            }
            .subscribeOn(Schedulers.io())
    }


    @Synchronized
    fun updateMarkers(file: File, markers: List<AudioMarker>) {

        if (isBouncingAudio) {
            updateStatus(TaskRunnerStatus.UPDATE_MARKERS_WAITING)

            status
                .takeWhile { status ->
                    status == TaskRunnerStatus.UPDATE_MARKERS_WAITING || status == TaskRunnerStatus.BOUNCING_AUDIO
                }
                .doOnComplete {
                    waitingMarkerUpdateTask = null
                    currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()
                }
                .doOnDispose {
                    logger.info("Cancelling stale update markers task")
                }
                .doOnSubscribe {
                    currentMarkerUpdateTask?.dispose()
                }
                .subscribe().let {
                    waitingMarkerUpdateTask?.dispose()
                    waitingMarkerUpdateTask = it
                }

        } else {
            updateStatus(TaskRunnerStatus.UPDATING_MARKERS)

            // Cancels the current marker update task (if any) since it now contains outdated markers
            currentMarkerUpdateTask?.dispose()

            currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()
        }

    }


    private fun updateMarkersTask(file: File, markers: List<AudioMarker>): Completable {
        return Completable
            .create { emitter ->
                logger.info("Started updating markers")
                this.currentUpdateMarkerTaskEmitter = emitter

                val oaf = OratureAudioFile(file)
                clearNarrationMarkers(oaf)
                addNarrationMarkers(oaf, markers)
                oaf.update()

                emitter.onComplete()
            }
            .doOnComplete {
                logger.info("Finished updating markers")
                // Emits IDLE because this is only reached if no new update marker or bounce audio tasks are received
                updateStatus(TaskRunnerStatus.IDLE)
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