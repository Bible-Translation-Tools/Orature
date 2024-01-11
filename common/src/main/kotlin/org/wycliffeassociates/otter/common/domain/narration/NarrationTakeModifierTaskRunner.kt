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

object NarrationTakeModifierTaskRunner {

    private val logger = LoggerFactory.getLogger(NarrationTakeModifierTaskRunner::class.java)

    private val audioBouncer = AudioBouncer()

    private var currentAudioBounceTask: Disposable? = null
    private lateinit var currentAudioBounceTaskEmitter: CompletableEmitter
    var audioBouncerBusy: Observable<Boolean>
    private var audioBouncerBusyEmitter: ObservableEmitter<Boolean>? = null
    private var isBouncingAudio = false

    private var currentMarkerUpdateTask: Disposable? = null
    private var currentUpdateMarkerTaskEmitter: CompletableEmitter? = null
    private var waitingMarkerUpdateTask: Disposable? = null
    var markerUpdateBusy: Observable<Boolean>
    private var markerUpdateBusyEmitter: ObservableEmitter<Boolean>? = null

    init {
        audioBouncerBusy = Observable.create { emitter ->
            this.audioBouncerBusyEmitter = emitter
        }

        markerUpdateBusy = Observable.create { emitter ->
            this.markerUpdateBusyEmitter = emitter
        }
    }

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
        audioBouncerBusyEmitter?.onNext(newValue)
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

            markerUpdateBusyEmitter?.onNext(true)

            audioBouncerBusy
                .takeWhile { busy -> busy }
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
                markerUpdateBusyEmitter?.onNext(true)
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
                markerUpdateBusyEmitter?.onNext(false)
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