/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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

import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import javax.inject.Inject
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.translation.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.domain.content.CreateChunks
import org.wycliffeassociates.otter.common.domain.content.ResetChunks
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.common.domain.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.observableListOf
import tornadofx.sizeProperty

const val WAV_COLOR = "#66768B"
const val BACKGROUND_COLOR = "#fff"

open class ChunkingViewModel : ViewModel(), IMarkerViewModel {

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

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()

    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)
    override var timer: AnimationTimer? = null
    override var sampleRate: Int = 0 // beware of divided by 0
    override var totalFrames: Int = 0 // beware of divided by 0

    lateinit var audio: OratureAudioFile
    lateinit var waveform: Observable<Image>
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)
    private val builder = ObservableWaveformBuilder()

    var subscribeOnWaveformImages: () -> Unit = {}

    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    open fun dock() {
        val chapter = workbookDataStore.chapter
        val sourceAudio = initializeSourceAudio(chapter.sort)
        audioDataStore.sourceAudioProperty.set(sourceAudio)

        sourceAudio?.file?.let {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            audio = loadAudio(it)
            createWaveformImages(audio)
            subscribeOnWaveformImages()
            loadChunkMarkers(audio)
        }
        startAnimationTimer()
    }

    open fun undock() {
        pause()
        translationViewModel.selectedStepProperty.value?.let {
            // handle when navigating to the next step
            val hasUnsavedChanges = markerCountProperty.value != 0 && markerModel?.hasDirtyMarkers() == true
            if (hasUnsavedChanges && it.ordinal > ChunkingStep.CHUNKING.ordinal) {
                saveChanges()
            }
            translationViewModel.updateStep()
        }

        if (markerModel?.hasDirtyMarkers() == true) {
            chapterReviewViewModel.invalidateChapterTake()
        }
        cleanup()
    }

    private fun initializeSourceAudio(chapter: Int): SourceAudio? {
        val workbook = workbookDataStore.workbook
        ChunkAudioUseCase(directoryProvider, workbook.projectFilesAccessor)
            .copySourceAudioToProject(sourceAudio.file)

        return workbook.sourceAudioAccessor.getUserMarkedChapter(chapter, workbook.target)
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
        if (!dirty) {
            translationViewModel.updateStep()
        }
    }

    override fun redoMarker() {
        super.redoMarker()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(markerModel?.canRedo() == true)
        translationViewModel.reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
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
        return audio
    }

    private fun loadChunkMarkers(audio: OratureAudioFile) {
        markers.clear()
        val totalMarkers = 500
        audio.clearCues()
        val chunkMarkers = audio.getMarker<ChunkMarker>().map {
            ChunkMarkerModel(AudioCue(it.location, it.label))
        }
        markers.setAll(chunkMarkers)
        markerModel = VerseMarkerModel(
            audio,
            totalMarkers,
            (1..totalMarkers).map { it.toString() }
        ).apply {
            loadMarkers(chunkMarkers)
        }
    }

    fun cleanup() {
        builder.cancel()
        compositeDisposable.clear()
        stopAnimationTimer()
        markerModel = null
    }

    fun saveChanges() {
        compositeDisposable.clear()
        audioConnectionFactory.clearPlayerConnections()
        waveformAudioPlayerProperty.value.close()
        audioController = null

        val accessor = workbookDataStore.workbook.projectFilesAccessor
        val wkbk = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        val cues = markers.filter { it.placed }.map { it.toAudioCue() }

        resetChunks.resetChapter(accessor, chapter)
        createChunks.createUserDefinedChunks(wkbk, chapter, cues, 2)

        ChunkAudioUseCase(directoryProvider, accessor)
            .createChunkedSourceAudio(sourceAudio.file, cues)
    }

    fun initializeAudioController(slider: Slider? = null) {
        audioController = AudioPlayerController(slider).also { controller ->
            waveformAudioPlayerProperty.value?.let {
                controller.load(it)
            }
        }
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
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
        // any changes in chunking will affect the subsequent steps
        translationViewModel.reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
    }
}
