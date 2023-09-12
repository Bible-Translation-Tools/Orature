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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.io.File
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import javax.inject.Inject
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.chunking.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.domain.content.CreateChunks
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.observableListOf
import tornadofx.sizeProperty
import kotlin.math.max

const val ACTIVE = "chunking-wizard__step--active"
const val COMPLETE = "chunking-wizard__step--complete"
const val INACTIVE = "chunking-wizard__step--inactive"

const val WAV_COLOR = "#66768B"
const val BACKGROUND_COLOR = "#fff"

class ChunkingViewModel() : ViewModel(), IMarkerViewModel {

    var timer: AnimationTimer? = null

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    val consumeStepColor = SimpleStringProperty(ACTIVE)
    val verbalizeStepColor = SimpleStringProperty(INACTIVE)
    val chunkStepColor = SimpleStringProperty(INACTIVE)

    val chapterTitle get() = workbookDataStore.activeChapterProperty.value?.title ?: ""
    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio by audioDataStore.sourceAudioProperty

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    @Inject
    lateinit var createChunks: CreateChunks

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()

    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)

    private val builder = ObservableWaveformBuilder()
    lateinit var waveform: Observable<Image>

    /** Call this before leaving the view to avoid memory leak */
    var chunkImageCleanup: () -> Unit = {}
    var consumeImageCleanup: () -> Unit = {}

    override var audioController: AudioPlayerController? = null
    override val audioPlayer = SimpleObjectProperty<IAudioPlayer>()
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)
    val changeUnsaved = SimpleBooleanProperty(false)
    private val disposeables = mutableListOf<Disposable>()

    lateinit var audio: OratureAudioFile

    var subscribeOnWaveformImages: () -> Unit = {}

    private var sampleRate: Int = 0 // beware of divided by 0
    private var sourceTotalFrames: Int = 0 // beware of divided by 0

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDockChunking() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        val sourceAudio = wb.sourceAudioAccessor.getUserMarkedChapter(chapter.sort, wb.target)
            ?: initializeSourceAudio(chapter.sort)

        audioDataStore.sourceAudioProperty.set(sourceAudio)

        sourceAudio?.file?.let {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            audio = loadAudio(it)
            createWaveformImages(audio)
            subscribeOnWaveformImages()
            loadChunkMarkers(audio)
        }
        startAnimationTimer()

        translationViewModel.currentMarkerProperty.bind(currentMarkerNumberProperty)
    }

    private fun initializeSourceAudio(chapter: Int): SourceAudio? {
        val workbook = workbookDataStore.workbook
        ChunkAudioUseCase(directoryProvider, workbook.projectFilesAccessor)
            .copySourceAudioToProject(sourceAudio.file)

        return workbook.sourceAudioAccessor.getUserMarkedChapter(chapter, workbook.target)
    }

    fun onUndockChunking() {
        pause()
        cleanup()
        translationViewModel.selectedStepProperty.value?.let {
            onNavigateStep(it)
        }
        translationViewModel.currentMarkerProperty.unbind()
        translationViewModel.currentMarkerProperty.set(-1)
        markerModel = null
    }

    fun onUndockChunk() {
        pause()
        compositeDisposable.clear()
        stopAnimationTimer()
    }

    private fun startAnimationTimer() {
        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                calculatePosition()
            }
        }.apply { start() }
    }

    private fun stopAnimationTimer() {
        timer?.stop()
        timer = null
    }

    fun loadAudio(audioFile: File): OratureAudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            sourceTotalFrames = it.totalFrames
        }
        audioPlayer.set(player)
        return audio
    }

    fun loadChunkMarkers(audio: OratureAudioFile) {
        markers.clear()

        workbookDataStore.chapter
            .chunks
            .observeOnFx()
            .subscribe { chunks ->
                val totalMarkers: Int = 500
                audio.clearCues()
                if (chunks.isEmpty()) {
                    markerModel = VerseMarkerModel(audio, totalMarkers, listOf())
                    markerModel?.let { markerModel ->
                        markers.setAll(markerModel.markers)
                    }
                } else {
                    val chunkMarkers = audio.getMarker<ChunkMarker>().map {
                        ChunkMarkerModel(AudioCue(it.location, it.label))
                    }
                    markers.setAll(chunkMarkers)
                    markerModel = VerseMarkerModel(audio, totalMarkers, (1..totalMarkers).map { it.toString() })
                    chunkMarkers.forEach { markerModel!!.addMarker(it.frame) }
                    markerModel!!.changesSaved = true
                }
            }.addTo(compositeDisposable)
    }

    fun cleanup() {
        builder.cancel()
        consumeImageCleanup()
        chunkImageCleanup()
        compositeDisposable.clear()
        stopAnimationTimer()
        disposeables.forEach { it.dispose() }
    }

    fun saveChanges() {
        compositeDisposable.clear()
        audioConnectionFactory.clearPlayerConnections()
        audioPlayer.value.close()
        audioController = null


        val accessor = workbookDataStore.workbook.projectFilesAccessor
        val wkbk = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        val cues = markers.filter { it.placed }.map { it.toAudioCue() }

        createChunks.createUserDefinedChunks(wkbk, chapter, cues, 2)

        ChunkAudioUseCase(directoryProvider, accessor)
            .createChunkedSourceAudio(sourceAudio.file, cues)

        markerModel?.changesSaved = true
        changeUnsaved.value = false
    }

    fun initializeAudioController(slider: Slider? = null) {
        audioController = AudioPlayerController(slider).also { controller ->
            audioPlayer.value?.let {
                controller.load(it)
            }
        }
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun onNavigateStep(step: ChunkingStep) {
        // handle when moving to the next step
        val hasUnsavedChanges = changeUnsaved.value || markerModel?.changesSaved == false
        if (hasUnsavedChanges && step.ordinal > ChunkingStep.CHUNKING.ordinal) {
            saveChanges()
        }
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(SECONDS_ON_SCREEN))

        waveform = builder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = height,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )
    }

    fun computeImageWidth(secondsOnScreen: Int): Double {
        val samplesPerScreenWidth = audioPlayer.get().getAudioReader()!!.sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        val pixelsInDuration = audioPlayer.get().getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }


    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || sourceTotalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = sourceTotalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }
}
