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
import com.github.thomasnield.rxkotlinfx.subscribeOnFx
import com.jakewharton.rxrelay2.BehaviorRelay
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.getWaveformColors
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.ChapterTranslationBuilder
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementType
import org.wycliffeassociates.otter.common.domain.model.UndoableActionHistory
import org.wycliffeassociates.otter.common.domain.translation.AddMarkerAction
import org.wycliffeassociates.otter.common.domain.translation.DeleteMarkerAction
import org.wycliffeassociates.otter.common.domain.translation.MoveMarkerAction
import org.wycliffeassociates.otter.common.domain.translation.TakeEditAction
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.SnackBarEvent
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import java.time.LocalDate
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

    val settingsViewModel: SettingsViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()
    private val translationViewModel: TranslationViewModel2 by inject()
    private val navigator: NavigationMediator by inject()

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
    val initWaveformMarkerProperty = SimpleObjectProperty {}

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
    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    val pluginOpenedProperty = SimpleBooleanProperty(false)
    private val actionHistory = UndoableActionHistory<IUndoable>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        translationViewModel.pluginOpenedProperty.bind(pluginOpenedProperty)
    }

    fun dock() {
        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        workbookDataStore.activeChunkProperty.set(null)
        translationViewModel.currentMarkerProperty.bind(currentMarkerNumberProperty)

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
        if (!pluginOpenedProperty.value) {
            audioDataStore.closePlayers()
        }
        waveformAudioPlayerProperty.value?.stop()
        waveformAudioPlayerProperty.value?.close()
        markerModel
            ?.writeMarkers()
            ?.blockingAwait()

        translationViewModel.currentMarkerProperty.unbind()
        translationViewModel.currentMarkerProperty.set(-1)
        cleanup()
    }

    fun onThemeChange() {
        reloadAudio(true).subscribe()
    }

    override fun placeMarker() {
        val location = waveformAudioPlayerProperty.get().getLocationInFrames()
        val action = AddMarkerAction(markerModel!!, location)
        actionHistory.execute(action)
        onUndoableAction()
    }

    override fun deleteMarker(id: Int) {
        val action = DeleteMarkerAction(markerModel!!, id)
        actionHistory.execute(action)
        onUndoableAction()
    }

    override fun moveMarker(id: Int, start: Int, end: Int) {
        val action = MoveMarkerAction(markerModel!!, id, start, end)
        actionHistory.execute(action)
        onUndoableAction()
    }

    fun undo() {
        actionHistory.undo()
        markers.setAll(markerModel!!.markerItems.toList())

        val dirty = actionHistory.canUndo()
        translationViewModel.canUndoProperty.set(dirty)
        translationViewModel.canRedoProperty.set(true)
    }

    fun redo() {
        actionHistory.redo()
        markers.setAll(markerModel!!.markerItems.toList())

        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(actionHistory.canRedo())
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

    fun processWithPlugin() {
        val chapter = workbookDataStore.chapter
        val existingChapterTake = chapter.audio.getSelectedTake()!!
        val oldMarkerModel = markerModel

        // save current markers before sending to plugin
        waveformAudioPlayerProperty.value?.stop()
        waveformAudioPlayerProperty.value?.close()
        markerModel
            ?.writeMarkers()
            ?.blockingAwait()

        createDuplicateTake()
            .observeOnFx()
            .subscribe { newTake ->
                workbookDataStore.activeTakeNumberProperty.set(newTake.number)
                chapter.audio.insertTake(newTake) // take must be inserted before editing

                val pluginType = PluginType.EDITOR
                audioPluginViewModel
                    .getPlugin(pluginType)
                    .doOnError { e ->
                        logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
                        e.printStackTrace()
                    }
                    .flatMapSingle { plugin ->
                        pluginOpenedProperty.set(true)
                        fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                        audioPluginViewModel.edit(chapter.audio, newTake)
                    }
                    .observeOnFx()
                    .doOnError { e ->
                        logger.error("Error in processing take with plugin type: $pluginType - $e")
                        e.printStackTrace()
                    }
                    .onErrorReturn { PluginActions.Result.NO_PLUGIN }
                    .subscribe { result: PluginActions.Result ->
                        logger.info("Returned from plugin with result: $result")
                        when (result) {
                            PluginActions.Result.NO_PLUGIN -> FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                            PluginActions.Result.SUCCESS -> {
                                val action = TakeEditAction(
                                    chapter.audio,
                                    newTake,
                                    existingChapterTake
                                ).apply {
                                    setupUndoRedo(this, oldMarkerModel)
                                }
                                actionHistory.execute(action)
                            }

                            else -> {
                                // no-op
                            }
                        }
                        FX.eventbus.fire(PluginClosedEvent(pluginType))
                    }
            }
    }

    fun snackBarMessage(message: String) {
        snackBarObservable.onNext(message)
    }

    private fun loadChapterTake() {
        navigator.blockNavigationEvents.set(true)

        chapterTranslationBuilder
            .getOrCompile(
                workbookDataStore.workbook,
                workbookDataStore.chapter
            )
            .flatMap { take ->
                loadTargetAudio(take)
            }
            .flatMapCompletable { audio ->
                loadMarkersAndWaveform(audio)
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                translationViewModel.loadingStepProperty.set(false)
                navigator.blockNavigationEvents.set(false)
            }
            .subscribe()
    }

    fun reloadAudio(keepMarkers: Boolean = false): Completable {
        cleanupWaveform()
        initWaveformMarkerProperty.value?.invoke()

        val chapterTake = workbookDataStore.chapter.audio.getSelectedTake()!!

        return loadTargetAudio(chapterTake)
            .flatMapCompletable {
                loadMarkersAndWaveform(it, keepMarkers)
            }
            .doOnComplete {
                onUndoableAction()
            }
    }

    private fun loadMarkersAndWaveform(audio: OratureAudioFile, keepMarkers: Boolean = false): Completable {
        return Completable
            .fromAction {
                val sourceAudio = audioDataStore.sourceAudioProperty.value
                    ?.let { OratureAudioFile(it.file) }
                if (!keepMarkers) {
                    loadVerseMarkers(audio, sourceAudio)
                }
                createWaveformImages(audio)
                subscribeOnWaveformImages()
            }
            .subscribeOnFx()
    }

    private fun loadTargetAudio(take: Take): Single<OratureAudioFile> {
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

    private fun createDuplicateTake(): Single<Take> {
        return workbookDataStore.chapter.let { chapter ->
            val namer = WorkbookFileNamerBuilder.createFileNamer(
                workbook = workbookDataStore.workbook,
                chapter = workbookDataStore.chapter,
                chunk = null,
                recordable = chapter,
                rcSlug = workbookDataStore.workbook.sourceMetadataSlug
            )
            val chapterAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir
                .resolve(namer.formatChapterNumber())
                .apply { mkdirs() }

            chapter.audio.getNewTakeNumber()
                .map { takeNumber ->
                    createNewTake(
                        takeNumber,
                        namer.generateName(takeNumber, AudioFileFormat.WAV),
                        chapterAudioDir
                    )
                }
        }
    }

    private fun createNewTake(
        newTakeNumber: Int,
        filename: String,
        audioDir: File
    ): Take {
        val newTakeFile = audioDir.resolve(File(filename))
        val chapterTake = workbookDataStore.chapter.getSelectedTake()!!
        chapterTake.file.copyTo(newTakeFile, true)

        val newTake = Take(
            name = newTakeFile.name,
            file = newTakeFile,
            number = newTakeNumber,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now(),
            checkingState = BehaviorRelay.createDefault(
                TakeCheckingState(CheckingStatus.VERSE, chapterTake.getSavedChecksum())
            )
        )
        return newTake
    }

    private fun setupUndoRedo(action: TakeEditAction, oldMarkerModel: MarkerPlacementModel?) {
        with(action) {
            setUndoCallback {
                newMarkerModel = markerModel!!

                reloadAudio()
                    .doOnComplete {
                        markerModel = oldMarkerModel
                    }
                    .subscribe()
            }
            setRedoCallback {
                reloadAudio()
                    .doOnComplete {
                        markerModel = newMarkerModel
                        onUndoableAction()
                    }
                    .subscribe()
            }
        }
    }

    private fun cleanup() {
        builder.cancel()
        actionHistory.clear()
        compositeDisposable.clear()
        markerModel = null
        cleanupWaveform()
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
                wavColor = waveformColors.wavColor,
                background = waveformColors.backgroundColor
            )
        }
    }

    private fun onUndoableAction() {
        markers.clear()
        markers.setAll(markerModel!!.markerItems.toList())

        translationViewModel.canUndoProperty.set(actionHistory.canUndo())
        translationViewModel.canRedoProperty.set(actionHistory.canRedo())
    }
}
