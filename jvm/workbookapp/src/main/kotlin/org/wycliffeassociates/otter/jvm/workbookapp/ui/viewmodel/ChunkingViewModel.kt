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
import java.io.File
import javafx.animation.AnimationTimer
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javax.inject.Inject
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.chunking.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.domain.content.CreateChunks
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
import tornadofx.get
import tornadofx.getValue
import tornadofx.observableListOf
import tornadofx.onChange
import tornadofx.sizeProperty
import kotlin.math.max

const val ACTIVE = "chunking-wizard__step--active"
const val COMPLETE = "chunking-wizard__step--complete"
const val INACTIVE = "chunking-wizard__step--inactive"

private const val WAV_COLOR = "#66768B"
private const val BACKGROUND_COLOR = "#fff"

enum class ChunkingWizardPage {
    CONSUME,
    VERBALIZE,
    CHUNK
}


class ChunkingViewModel() : ViewModel(), IMarkerViewModel {

    var timer: AnimationTimer? = null

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val consumeStepColor = SimpleStringProperty(ACTIVE)
    val verbalizeStepColor = SimpleStringProperty(INACTIVE)
    val chunkStepColor = SimpleStringProperty(INACTIVE)

    val chapterTitle get() = workbookDataStore.activeChapterProperty.value?.title ?: ""
    val pageProperty = SimpleObjectProperty(ChunkingWizardPage.CONSUME)
    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")
    val selectedChunk: IntegerProperty = SimpleIntegerProperty(2)
    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(null)
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)

    val sourceAudio by audioDataStore.sourceAudioProperty
    val sourceTextProperty = SimpleStringProperty()

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

    private val disposeables = mutableListOf<Disposable>()

    lateinit var audio: OratureAudioFile

    var subscribeOnWaveformImages: () -> Unit = {}

    private var sampleRate: Int = 0 // beware of divided by 0
    private var sourceTotalFrames: Int = 0 // beware of divided by 0

    fun dockPage() {
        val recentChapter = workbookDataStore.workbookRecentChapterMap.getOrDefault(
            workbookDataStore.workbook.hashCode(),
            1
        )
        val chapter = workbookDataStore.workbook.target.chapters
            .filter { it.sort == recentChapter }
            .blockingFirst()

        workbookDataStore.activeChapterProperty.set(chapter)
        workbookDataStore.getSourceText()
            .observeOnFx()
            .subscribe {
                sourceTextProperty.set(it)
            }
    }

    fun onDockConsume() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        val sourceAudio = wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
        audioDataStore.sourceAudioProperty.set(sourceAudio)

        sourceAudio?.file?.let {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            audio = loadAudio(it)
            createWaveformImages(audio)
            subscribeOnWaveformImages()
            loadMarkers(audio)
        }
        startAnimationTimer()
    }

    fun onUndockConsume() {
        pause()
        cleanup()
    }

    fun onDockChunk() {
        titleProperty.set(messages["chunkingTitle"])
        stepProperty.set(messages["chunkingDescription"])

        loadMarkers(audio)
        subscribeOnWaveformImages()
        startAnimationTimer()
        seek(0)
    }

    fun onDockChunking() {
        onDockConsume()
    }

    fun onUndockChunking() {
        onUndockConsume()
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

    fun loadMarkers(audio: OratureAudioFile) {
        val totalMarkers: Int = 500
        audio.clearCues()
        val marketLabels = workbookDataStore.getSourceChapter().map { it.getDraft() }.blockingGet().map { it.title }.toList().blockingGet()
        markerModel = VerseMarkerModel(audio, marketLabels.size, marketLabels)
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }

    fun cleanup() {
        builder.cancel()
        consumeImageCleanup()
        chunkImageCleanup()
        compositeDisposable.clear()
        stopAnimationTimer()
        disposeables.forEach { it.dispose() }
    }

    fun saveAndQuit() {
        compositeDisposable.clear()
        audioConnectionFactory.clearPlayerConnections()
        audioPlayer.value.close()
        audioController = null

        val accessor = workbookDataStore.workbook.projectFilesAccessor
        val wkbk = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        val cues = markers.filter { it.placed }.map { it.toAudioCue() }

        createChunks.createUserDefinedChunks(wkbk, chapter, cues, 1)

        pageProperty.set(ChunkingWizardPage.CONSUME)

        ChunkAudioUseCase(directoryProvider, accessor)
            .createChunkedSourceAudio(sourceAudio.file, cues)

        disposeables.forEach { it.dispose() }
    }

    fun initializeAudioController(slider: Slider? = null) {
        audioController = AudioPlayerController(slider)
        audioController?.load(audioPlayer.get())
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
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
