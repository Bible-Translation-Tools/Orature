package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioBouncer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.lang.Error
import java.util.concurrent.atomic.AtomicBoolean

object NarrationAudioBouncerTaskRunner {

    private val logger = LoggerFactory.getLogger(NarrationAudioBouncerTaskRunner::class.java)

    private val audioBouncer = AudioBouncer()
    private var currentAudioBounceTask: Disposable? = null
    private lateinit var currentAudioBounceTaskEmitter: CompletableEmitter

    private var currentMarkerUpdateTask: Disposable? = null
    private lateinit var currentUpdateMarkerTaskEmitter: CompletableEmitter

    var audioBouncerBusy: Observable<Boolean>
    private var audioBouncerBusyEmitter: ObservableEmitter<Boolean>? = null
    var markerUpdateBusy: Observable<Boolean>
    private var markerUpdateBusyEmitter: ObservableEmitter<Boolean>? = null

    private val isBusy = AtomicBoolean(false)


    init {
        audioBouncerBusy = Observable.create { emitter ->
            this.audioBouncerBusyEmitter = emitter
        }

        markerUpdateBusy = Observable.create { emitter ->
            this.markerUpdateBusyEmitter = emitter
        }
    }


    fun bounce(file: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        cancelPreviousAudioBounceTask()
        currentAudioBounceTask = bounceAudioTask(file, reader, markers).subscribe()
    }


    private fun cancelPreviousAudioBounceTask() {
        if (currentAudioBounceTask != null) {
            audioBouncer.interrupt()
            currentAudioBounceTaskEmitter.onComplete()
        }
    }

    private fun bounceAudioTask(
        file: File,
        reader: AudioFileReader,
        markers: List<AudioMarker>
    ): Completable {

        return Completable
            .create { emitter ->
                audioBouncerBusyEmitter?.onNext(true)
                isBusy.set(true)
                this.currentAudioBounceTaskEmitter = emitter

                audioBouncer.bounceAudio(file, reader, markers)

                emitter.onComplete()
            }
            .doOnError {
                logger.error("Error occurred while bouncing audio")
            }
            .doFinally {
                audioBouncerBusyEmitter?.onNext(false)
                isBusy.set(false)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun updateMarkersTask(file: File, markers: List<AudioMarker>): Completable {
        return Completable
            .create { emitter ->
                markerUpdateBusyEmitter?.onNext(true)
                isBusy.set(true)
                currentUpdateMarkerTaskEmitter = emitter

                val oaf = OratureAudioFile(file)
                clearNarrationMarkers(oaf)
                addNarrationMarkers(oaf, markers)
                oaf.update()

                emitter.onComplete()
            }
            .doOnError {
                logger.error(it.toString())
            }
            .doFinally {
                isBusy.set(false)
                markerUpdateBusyEmitter?.onNext(false)
            }
            .subscribeOn(Schedulers.io())

    }

    fun updateMarkers(file: File, markers: List<AudioMarker>) {
        cancelPreviousMarkerUpdateTask()

        // If isBusy is true, then wait until bounceAudioBusy is false, otherwise, go ahead and update markers
        if (isBusy.compareAndSet(false, true)) {
            currentMarkerUpdateTask = updateMarkersTask(file, markers).subscribe()
        } else {
            audioBouncerBusy
                .filter { busy -> !busy }
                .firstOrError()
                .flatMapCompletable {
                    updateMarkersTask(file, markers)
                }
                .doOnSubscribe {
                    currentMarkerUpdateTask = it
                }
                .doOnError {
                    logger.error(it.toString())
                }
                .subscribe()
        }

    }

    private fun cancelPreviousMarkerUpdateTask() {
        if (currentMarkerUpdateTask != null) {
            currentUpdateMarkerTaskEmitter.onComplete()
        }
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