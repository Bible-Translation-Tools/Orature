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

    private var busyStatusEmitter: ObservableEmitter<TaskRunnerStatus>? = null
    val busyStatus: ConnectableObservable<TaskRunnerStatus> = Observable
        .create {
            busyStatusEmitter = it
        }.publish()
    private var isBouncingAudio = false

    init {
        busyStatus.connect()
    }

    @Synchronized
    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        updateBusyStatus(TaskRunnerStatus.BOUNCING_AUDIO)

        waitingMarkerUpdateTask?.dispose()
        waitingMarkerUpdateTask = null
        currentAudioBounceTask?.dispose()

        cancelPreviousAudioBounceTask()
        currentAudioBounceTask = bounceAudioTask(file, reader, markers).subscribe()
    }


    private fun updateBusyStatus(status: TaskRunnerStatus) {
        isBouncingAudio = (status == TaskRunnerStatus.BOUNCING_AUDIO)
        busyStatusEmitter?.onNext(status)
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
                    if (waitingMarkerUpdateTask == null) {
                        // Only set busy status to IDLE when we do not have a waiting update marker task
                        updateBusyStatus(TaskRunnerStatus.IDLE)
                    } else {
                        // Sets status to UPDATING_MARKERS so the waiting update marker task can start executing
                        updateBusyStatus(TaskRunnerStatus.UPDATING_MARKERS)
                    }
                }
            }
            .subscribeOn(Schedulers.io())
    }


    @Synchronized
    fun updateMarkers(file: File, markers: List<AudioMarker>) {

        if (!isBouncingAudio) {
            updateBusyStatus(TaskRunnerStatus.UPDATING_MARKERS)
            currentMarkerUpdateTask?.dispose()
            currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()

        } else {

            updateBusyStatus(TaskRunnerStatus.UPDATE_MARKERS_WAITING)

            busyStatus
                .takeWhile { busy ->
                    busy == TaskRunnerStatus.UPDATE_MARKERS_WAITING || busy == TaskRunnerStatus.BOUNCING_AUDIO
                }
                .doOnComplete {
                    waitingMarkerUpdateTask = null
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
                // Can emit IDLE at this point, because we only reach this if we have not been interrupted by another
                // bounce audio task or update marker task.
                updateBusyStatus(TaskRunnerStatus.IDLE)
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