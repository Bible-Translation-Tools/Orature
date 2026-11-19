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
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.getWaveformColors
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import javax.inject.Inject
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.translation.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.domain.content.CreateChunks
import org.wycliffeassociates.otter.common.domain.content.ResetChunks
import org.wycliffeassociates.otter.common.domain.model.DEFAULT_CHUNK_MARKER_TOTAL
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementType
import org.wycliffeassociates.otter.jvm.controls.dialog.ConfirmDialog
import org.wycliffeassociates.otter.jvm.controls.event.AppCloseRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.ChunkingStepTransitionEvent
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import tornadofx.*

/**
 * How long to wait after the last marker change before saving the chunks.
 */
private const val AUTO_SAVE_DEBOUNCE_MS = 750L

class ChunkingViewModel : ViewModel(), IMarkerViewModel {
    private val logger = LoggerFactory.getLogger(ChunkingViewModel::class.java)

    val settingsViewModel: SettingsViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val chapterReviewViewModel: ChapterReviewViewModel by inject()

    val chapterTitle get() = workbookDataStore.activeChapterProperty.value?.title ?: ""
    val sourceAudio by audioDataStore.sourceAudioProperty

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    @Inject
    lateinit var createChunks: CreateChunks

    @Inject
    lateinit var resetChunks: ResetChunks

    override var markerModel: MarkerPlacementModel? = null
    override val markers = observableListOf<MarkerItem>()

    override val markerCountProperty = markers.sizeProperty
    override val highlightedMarkerIndexProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override val audioPositionProperty = SimpleIntegerProperty()
    override val imageWidthProperty = SimpleDoubleProperty(0.0)
    override var sampleRate: Int = 0 // beware of divided by 0
    override val totalFramesProperty = SimpleIntegerProperty(0)
    override var totalFrames: Int by totalFramesProperty // beware of divided by 0

    private var audio: OratureAudioFile? = null
    lateinit var waveform: Observable<Image>
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, WAVEFORM_MAX_HEIGHT.toInt())
    private val builder = ObservableWaveformBuilder()

    var subscribeOnWaveformImagesProperty = SimpleObjectProperty {}
    val cleanupWaveformProperty = SimpleObjectProperty {}

    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()

    /**
     * Everything a save needs to know about the chapter being chunked, captured when chunking opens so that a save
     * that finishes after the user has moved on still writes to the chapter it was made for.
     */
    private data class ChunkingSession(
        val workbook: Workbook,
        val chapter: Chapter,
        /** The chapter source audio in the project; chunk markers are saved here. */
        val projectSourceAudio: File,
        /** A temp copy of [projectSourceAudio] that the player and waveform read, so saves can write the original. */
        val workingCopy: File,
        val sourceVerseMarkers: List<AudioCue>?,
        /**
         * Saving chunks deletes the chapter's recordings, so chunks are only saved automatically while the chapter
         * has none. Otherwise, saving waits until the user confirms moving on to the next step.
         */
        val autoSave: Boolean
    )

    private data class PendingSave(val session: ChunkingSession, val cues: List<AudioCue>)

    private var session: ChunkingSession? = null

    /**
     * Saves run one at a time on this thread. Since each save replaces all of the chapter's chunks, only the newest
     * snapshot matters: it waits in [pendingSave], and a snapshot that arrives while a save is running replaces it.
     */
    private val saveExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "chunk-auto-save").apply { isDaemon = true }
    }
    private val pendingSave = AtomicReference<PendingSave?>(null)
    private val saveRequests = PublishSubject.create<Unit>()
    private val autoSaveDisposable = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        subscribe<AppCloseRequestEvent> {
            logger.info("Received close event request, saving any pending chunks")
            flushPendingSave()
        }
    }

    fun dock() {
        val workbook = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter

        initializeSourceAudio(chapter.sort)
            .map { sa -> sa to startSession(workbook, chapter, sa.file) }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                translationViewModel.loadingStepProperty.set(false)
            }
            .subscribe({ (sa, newSession) ->
                audioDataStore.sourceAudioProperty.set(sa)
                session = newSession
                audio = loadAudio(newSession.workingCopy)
                audio?.let {
                    createWaveformImages(it)
                    loadChunkMarkers(it)
                    subscribeOnWaveformImages()
                }
                if (newSession.autoSave) {
                    startAutoSave()
                }
            }, { error ->
                logger.error("Error loading source audio for chunking chapter ${chapter.sort}", error)
            })
    }

    fun undock() {
        pause()
        stopAutoSave()

        val step = translationViewModel.selectedStepProperty.value
        val autoSave = session?.autoSave == true
        // When chunks aren't saved automatically, they are saved only when moving on to a later step
        if (!autoSave && step != null && hasUnsavedChanges() && step.ordinal > ChunkingStep.CHUNKING.ordinal) {
            requestSave()
        }
        flushPendingSave()

        step?.let {
            translationViewModel.updateStep()
        }

        if (markerModel?.canUndo() == true) {
            chapterReviewViewModel.invalidateChapterTake()
        }
        cleanup()
    }

    private fun initializeSourceAudio(chapter: Int): Maybe<SourceAudio> {
        return Maybe.fromCallable {
            val workbook = workbookDataStore.workbook
            workbook.sourceAudioAccessor.getUserMarkedChapter(chapter, workbook.target)  // this first call is intentional. See https://github.com/Bible-Translation-Tools/Orature/pull/1186
                ?: let {
                    ChunkAudioUseCase(directoryProvider, workbook.projectFilesAccessor)
                        .copySourceAudioToProject(sourceAudio.file)

                    workbook.sourceAudioAccessor.getUserMarkedChapter(chapter, workbook.target)
                }
        }
    }

    /**
     * Runs on a background thread: copies the source audio for playback and loads what saves need.
     */
    private fun startSession(workbook: Workbook, chapter: Chapter, projectSourceAudio: File): ChunkingSession {
        val workingCopy = ChunkAudioUseCase(directoryProvider, workbook.projectFilesAccessor)
            .createWorkingCopy(projectSourceAudio)

        val sourceVerseMarkers = try {
            createChunks.getSourceVerseMarkers(workbook, chapter.sort)
        } catch (e: Exception) {
            logger.error("Could not load source verse markers for chapter ${chapter.sort}", e)
            null // createUserDefinedChunks will try loading them again when saving
        }

        val hasRecordings = chapter.chunks.blockingGet().any { chunk ->
            chunk.audio.getAllTakes().any { take -> take.deletedTimestamp.value?.value == null }
        }

        return ChunkingSession(
            workbook = workbook,
            chapter = chapter,
            projectSourceAudio = projectSourceAudio,
            workingCopy = workingCopy,
            sourceVerseMarkers = sourceVerseMarkers,
            autoSave = !hasRecordings
        )
    }

    fun onThemeChange() {

        // Avoids null error in createWaveformImages cause by player not yet being initialized.
        val hasAudioAndPlayer =
            waveformAudioPlayerProperty.value != null && waveformAudioPlayerProperty.value.getDurationInFrames() > 0

        if (!hasAudioAndPlayer) {
            return
        }

        audio?.let {
            pause()
            builder.cancel()
            cleanupWaveform()
            createWaveformImages(it)
            subscribeOnWaveformImages()
        }
    }

    override fun placeMarker() {
        super.placeMarker()
        onUndoableAction()
        onMarkersChanged()
    }

    override fun deleteMarker(id: Int) {
        super.deleteMarker(id)
        onUndoableAction()
        onMarkersChanged()
    }

    override fun moveMarker(id: Int, start: Int, end: Int) {
        super.moveMarker(id, start, end)
        onUndoableAction()
        onMarkersChanged()
    }

    override fun undoMarker() {
        super.undoMarker()
        val dirty = markerModel?.canUndo() ?: false
        translationViewModel.canUndoProperty.set(dirty)
        translationViewModel.canRedoProperty.set(true)
        if (!dirty) {
            translationViewModel.updateStep()
        }
        onMarkersChanged()
    }

    override fun redoMarker() {
        super.redoMarker()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(markerModel?.canRedo() == true)
        translationViewModel.reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
        onMarkersChanged()
    }

    private fun loadAudio(audioFile: File): OratureAudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }

        waveformAudioPlayerProperty.set(player)
        audioController = AudioPlayerController().also { controller ->
            controller.load(player)
            isPlayingProperty.bind(controller.isPlayingProperty)
        }
        return audio
    }

    private fun loadChunkMarkers(audio: OratureAudioFile) {
        markers.clear()
        val totalMarkers = DEFAULT_CHUNK_MARKER_TOTAL
        audio.clearCues()
        val chunkMarkers = audio.getMarker<ChunkMarker>().map {
            MarkerItem(it, true)
        }
        markers.setAll(chunkMarkers)
        markerModel = MarkerPlacementModel(
            MarkerPlacementType.CHUNK,
            audio,
            (1..totalMarkers).map { ChunkMarker(it, 0) }
        ).apply {
            loadMarkers(chunkMarkers)
        }
    }

    fun cleanup() {
        audioConnectionFactory.releasePlayer()
        builder.cancel()
        compositeDisposable.clear()
        markerModel = null
        cleanupWaveform()
        closeWorkingCopy()
    }

    fun pause() {
        audioController?.pause()
    }

    fun cleanupWaveform() {
        cleanupWaveformProperty.value.invoke()
    }

    fun subscribeOnWaveformImages() {
        subscribeOnWaveformImagesProperty.value.invoke()
    }

    fun requestToNavigate(targetStep: ChunkingStep) {
        val chunkCount = workbookDataStore.chapter.chunkCount.blockingGet()
        // Chunks that are saved automatically have no recordings to lose, so there is nothing to warn about
        val wouldLoseRecordings = session?.autoSave != true && hasUnsavedChanges() && chunkCount > 0
        if (wouldLoseRecordings && targetStep.ordinal > ChunkingStep.CHUNKING.ordinal) {
            val dialog = find<ConfirmDialog> {
                titleTextProperty.set(messages["warning"])
                messageTextProperty.set(messages["rechunk_data_loss_warning"])
                confirmButtonTextProperty.set(messages["continue"])
                cancelButtonTextProperty.set(messages["cancel"])
                orientationProperty.set(settingsViewModel.orientationProperty.value)
                themeProperty.set(settingsViewModel.appColorMode.value)

                onConfirmAction {
                    this.close()
                    FX.eventbus.fire(ChunkingStepTransitionEvent(targetStep))
                }
                onCancelAction {
                    this.close()
                }
            }
            dialog.open()
        } else {
            FX.eventbus.fire(ChunkingStepTransitionEvent(targetStep))
        }
    }

    private fun onMarkersChanged() {
        if (session?.autoSave == true) {
            requestSave()
            saveRequests.onNext(Unit)
        }
    }

    /**
     * Records the current markers as the next save. Must be called on the FX thread, where the markers are changed.
     */
    private fun requestSave() {
        val currentSession = session ?: return
        val model = markerModel ?: return
        // The marker model is the source of truth; the observable list can hold copies of its items
        val cues = model.markerItems.filter { it.placed }.map { it.toAudioCue() }
        pendingSave.set(PendingSave(currentSession, cues))
    }

    private fun startAutoSave() {
        autoSaveDisposable.clear()
        saveRequests
            .debounce(AUTO_SAVE_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
            .subscribe {
                saveExecutor.execute(::savePending)
            }
            .addTo(autoSaveDisposable)
    }

    private fun stopAutoSave() {
        autoSaveDisposable.clear()
    }

    /**
     * Waits for any save that is running, then saves the pending markers, if any. Blocks the calling thread.
     */
    private fun flushPendingSave() {
        try {
            saveExecutor.submit(::savePending).get()
        } catch (e: Exception) {
            logger.error("Error while waiting for chunks to save", e)
        }
    }

    /**
     * Runs on [saveExecutor]. Never cancelled part way, since stopping between clearing and recreating the chunks
     * would leave the chapter without chunks.
     */
    private fun savePending() {
        val save = pendingSave.getAndSet(null) ?: return
        val session = save.session
        val accessor = session.workbook.projectFilesAccessor

        logger.info("Saving ${save.cues.size} chunks for chapter ${session.chapter.sort}")
        try {
            resetChunks.resetChapter(accessor, session.chapter)
                .andThen(
                    createChunks.createUserDefinedChunks(
                        session.workbook,
                        session.chapter,
                        save.cues,
                        session.sourceVerseMarkers
                    )
                )
                .blockingAwait()

            ChunkAudioUseCase(directoryProvider, accessor)
                .updateChunkMarkers(session.projectSourceAudio, save.cues)
        } catch (e: Exception) {
            logger.error("Error saving chunks for chapter ${session.chapter.sort}", e)
        }
    }

    /**
     * Closes the player and waveform reading the working copy, then deletes it.
     */
    private fun closeWorkingCopy() {
        val closingSession = session ?: return
        session = null

        waveformAudioPlayerProperty.value?.close()
        audioController = null
        audio = null

        ChunkAudioUseCase(directoryProvider, closingSession.workbook.projectFilesAccessor)
            .deleteWorkingCopy(closingSession.workingCopy)
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(width, SECONDS_ON_SCREEN))

        val waveformColors = getWaveformColors(settingsViewModel.appColorMode.value)

        waveformColors?.let {
            builder.cancel()
            waveform = builder.buildAsync(
                audio.reader(),
                width = imageWidthProperty.value.toInt(),
                height = height,
                wavColor = Color.web(waveformColors.wavColorHex),
                background = Color.web(waveformColors.backgroundColorHex)
            )
        }
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
        if (markers.size > 0) {
            // enable next step when chunks are placed
            translationViewModel.reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
        } else {
            translationViewModel.reachableStepProperty.set(ChunkingStep.CHUNKING)
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        return markerCountProperty.value != 0 && markerModel?.canUndo() == true
    }
}
