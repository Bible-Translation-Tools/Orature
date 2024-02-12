/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.ChapterTranslationBuilder
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.text.MessageFormat
import javax.inject.Inject
import kotlin.collections.sortBy

class ChapterReviewViewModel : ViewModel(), IMarkerViewModel {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var waveFileCreator: IWaveFileCreator

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    @Inject
    lateinit var chapterTranslationBuilder: ChapterTranslationBuilder

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    override var markerModel: MarkerPlacementModel? = null
    override val markers = observableListOf<MarkerItem>()

    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override val audioPositionProperty = SimpleIntegerProperty()
    override var imageWidthProperty = SimpleDoubleProperty(0.0)
    override val totalFramesProperty = SimpleIntegerProperty(0)
    override var totalFrames: Int by totalFramesProperty // beware of divided by 0
    override var sampleRate: Int = 0 // beware of divided by 0

    lateinit var waveform: Observable<Image>
    private val sourceAudio by audioDataStore.sourceAudioProperty
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, WAVEFORM_MAX_HEIGHT.toInt())
    private val builder = ObservableWaveformBuilder()

    var subscribeOnWaveformImagesProperty = SimpleObjectProperty {}
    val cleanupWaveformProperty = SimpleObjectProperty {}

    val chapterTitleProperty = workbookDataStore.activeChapterTitleBinding()
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val markerProgressCounterProperty = SimpleStringProperty()
    val totalMarkersProperty = SimpleIntegerProperty(0)
    val markersPlacedCountProperty = SimpleIntegerProperty(0)
    val canGoNextChapterProperty: BooleanBinding = translationViewModel.isLastChapterProperty.not().and(
        markersPlacedCountProperty.isEqualTo(totalMarkersProperty)
    )
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()


    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        workbookDataStore.activeChunkProperty.set(null)

        Completable
            .fromAction {
                audioDataStore.updateSourceAudio()
                audioDataStore.openSourceAudioPlayer()
            }
            .andThen(translationViewModel.updateSourceText())
            .subscribeOn(Schedulers.io())
            .subscribe()

        markersPlacedCountProperty.bind(markers.sizeProperty)
        markerProgressCounterProperty.bind(
            stringBinding(markersPlacedCountProperty, totalMarkersProperty) {
                MessageFormat.format(
                    messages["marker_placed_ratio"],
                    markersPlacedCountProperty.value ?: 0,
                    totalMarkersProperty.value ?: 0
                )
            }
        )

        loadChapterTake()
    }

    fun undock() {
        pauseAudio()
        audioDataStore.stopPlayers()
        audioDataStore.closePlayers()
        waveformAudioPlayerProperty.value?.stop()
        waveformAudioPlayerProperty.value?.close()
        markerModel
            ?.writeMarkers()
            ?.blockingAwait()

        cleanup()
    }

    override fun placeMarker() {
        super.placeMarker()
        onUndoableAction()
    }

    override fun deleteMarker(id: Int) {
        super.deleteMarker(id)
        onUndoableAction()
    }

    override fun moveMarker(id: Int, start: Int, end: Int) {
        super.moveMarker(id, start, end)
        onUndoableAction()
    }

    override fun undoMarker() {
        super.undoMarker()
        val dirty = markerModel?.hasDirtyMarkers() ?: false
        translationViewModel.canUndoProperty.set(dirty)
        translationViewModel.canRedoProperty.set(true)
    }

    override fun redoMarker() {
        super.redoMarker()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(markerModel?.canRedo() == true)
    }

    fun pauseAudio() = audioController?.pause()

    fun invalidateChapterTake() {
        workbookDataStore.chapter
            .audio
            .getSelectedTake()
            ?.let {
                it.checkingState.accept(
                    TakeCheckingState(CheckingStatus.UNCHECKED, null)
                )
                it.deletedTimestamp.accept(DateHolder.now())
            }
    }

    fun cleanupWaveform() {
        cleanupWaveformProperty.value.invoke()
    }

    fun subscribeOnWaveformImages() {
        subscribeOnWaveformImagesProperty.value.invoke()
    }

    private fun loadChapterTake() {
        chapterTranslationBuilder
            .getOrCompile(
                workbookDataStore.workbook,
                workbookDataStore.chapter
            )
            .flatMap { take ->
                loadTargetAudio(take)
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                translationViewModel.loadingStepProperty.set(false)
            }
            .subscribe { audio ->
                val sourceAudio = audioDataStore.sourceAudioProperty.value
                    ?.let { OratureAudioFile(it.file) }

                loadVerseMarkers(audio, sourceAudio)
                createWaveformImages(audio)
                subscribeOnWaveformImages()
            }
    }

    private fun loadTargetAudio(take: Take) : Single<OratureAudioFile> {
        return Single
            .fromCallable {
                val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
                audioPlayer.load(take.file)
                audioPlayer.getAudioReader()?.let {
                    sampleRate = it.sampleRate
                    totalFrames = it.totalFrames
                }
                audioController = AudioPlayerController().also { controller ->
                    controller.load(audioPlayer)
                    isPlayingProperty.bind(controller.isPlayingProperty)
                }
                waveformAudioPlayerProperty.set(audioPlayer)
                OratureAudioFile(take.file)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun loadVerseMarkers(audio: OratureAudioFile, sourceAudio: OratureAudioFile?) {
        markers.clear()
        val sourceMarkers = getSourceMarkers(sourceAudio)
        val placedMarkers = audio.getVerseAndTitleMarkers()
                .map { MarkerItem(it, true) }

        totalMarkersProperty.set(sourceMarkers.size)
        markerModel = MarkerPlacementModel(
            MarkerPlacementType.VERSE,
            audio,
            sourceMarkers.map { it.clone(0) }
        ).also {
            it.loadMarkers(placedMarkers)
        }
        markers.setAll(placedMarkers)
        markers.sortBy { it.frame }
    }

    private fun getSourceMarkers(sourceAudio: OratureAudioFile?): List<AudioMarker> {
        return when {
            sourceAudio != null && hasUserDefinedChunks() -> {
                sourceAudio.getVerseAndTitleMarkers()
            }

            /* no user-defined chunks found, this means project was migrated from Ot1, only have verse markers */
            sourceAudio != null -> sourceAudio.getMarker<VerseMarker>()

            else -> getMarkersFromText() // no source audio, create markers from text
        }
    }

    private fun hasUserDefinedChunks(): Boolean {
        val workbook = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        val chunkedAudio = workbook.sourceAudioAccessor
            .getUserMarkedChapter(chapter.sort, workbook.target)
            ?: return false

        val chunkMarkers = OratureAudioFile(chunkedAudio.file)
            .getMarker<ChunkMarker>()

        return chunkMarkers.isNotEmpty()
    }

    private fun getMarkersFromText(): List<VerseMarker> {
        return workbookDataStore.workbook.projectFilesAccessor
            .getChapterContent(
                workbookDataStore.workbook.target.slug,
                workbookDataStore.chapter.sort
            ).map { content ->
                VerseMarker(content.start, content.end, 0)
            }
    }

    private fun cleanup() {
        builder.cancel()
        compositeDisposable.clear()
        markerModel = null
        cleanupWaveform()
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(width, SECONDS_ON_SCREEN))

        waveform = builder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = height,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }
}
