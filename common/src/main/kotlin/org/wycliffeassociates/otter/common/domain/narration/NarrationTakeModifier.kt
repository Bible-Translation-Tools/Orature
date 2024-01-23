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
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File


enum class TaskRunnerStatus {
    MODIFYING_AUDIO,
    MODIFYING_METADATA,
    IDLE
}


// Manages audio and metadata modifications for chapter takes by fully executing the most relevant modification
object NarrationTakeModifier {

    private val logger = LoggerFactory.getLogger(NarrationTakeModifier::class.java)

    private val audioBouncer = AudioBouncer()

    private var currentAudioBounceTask: Disposable? = null
    private lateinit var currentAudioBounceTaskEmitter: CompletableEmitter

    private var currentMarkerUpdateTask: Disposable? = null
    private var currentUpdateMarkerTaskEmitter: CompletableEmitter? = null

    // Stores the most recent update marker task since bounce audio task must complete before metadata modification
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

    /**
     * Called when we need to update chapter take audio.
     *
     * Cancels any ongoing audio modifications and metadata modifications.
     *
     * @param take the take to modify
     * @param reader the reader to retrieve new audio data
     * @param markers the markers that correspond to the new audio data
     */
    @Synchronized
    fun modifyAudioData(take: Take?, reader: AudioFileReader, markers: List<AudioMarker>) {

        if (take == null) return

        val takeFile = take.file

        updateStatus(TaskRunnerStatus.MODIFYING_AUDIO)

        // Prevents waiting marker update task from executing after ongoing audio bounce tasks are canceled.
        cancelMarkerUpdateTask()

        cancelPreviousAudioBounceTask()
        currentAudioBounceTask = bounceAudioTask(takeFile, reader, markers).subscribe()
    }


    private fun updateStatus(status: TaskRunnerStatus) {
        isBouncingAudio =
            (status == TaskRunnerStatus.MODIFYING_AUDIO)
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
                        // Sets status to MODIFYING_METADATA so waiting update marker task can start executing
                        updateStatus(TaskRunnerStatus.MODIFYING_METADATA)
                    }
                }
            }
            .subscribeOn(Schedulers.io())
    }


    /**
     * Called when we need to update chapter take audio metadata.
     *
     * This happens for actions that involve chapter take audio metadata (i.e. moving a verse marker).
     *
     * Waits for any ongoing audio modifications to finish, before executing the most recent metadata modification
     *
     * @param take the take to modify
     * @param markers the markers that correspond to the audio data
     */
    @Synchronized
    fun modifyMetadata(take: Take?, markers: List<AudioMarker>) {

        if (take == null) return

        val takeFile = take.file

        if (isBouncingAudio) {
            status
                .takeWhile { status ->
                    status == TaskRunnerStatus.MODIFYING_AUDIO
                }
                .doOnComplete {
                    waitingMarkerUpdateTask = null
                    currentMarkerUpdateTask = updateMarkersTask(takeFile, markers).subscribe()
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
            updateStatus(TaskRunnerStatus.MODIFYING_METADATA)

            // Cancels the current marker update task (if any) since it now contains outdated markers
            currentMarkerUpdateTask?.dispose()

            currentMarkerUpdateTask = updateMarkersTask(takeFile, markers).subscribe()
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