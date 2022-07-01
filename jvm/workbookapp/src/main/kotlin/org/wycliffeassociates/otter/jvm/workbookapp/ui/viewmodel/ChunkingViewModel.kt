/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import javafx.beans.binding.IntegerBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javax.inject.Inject
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.VerseByVerseChunking
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.observableListOf
import tornadofx.onChange
import tornadofx.runLater
import tornadofx.sizeProperty

const val ACTIVE = "chunking-wizard__step--active"
const val COMPLETE = "chunking-wizard__step--complete"
const val INACTIVE = "chunking-wizard__step--inactive"

private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

enum class ChunkingWizardPage {
    CONSUME,
    VERBALIZE,
    CHUNK
}


class ChunkAudioUseCase(val directoryProvider: IDirectoryProvider, val workbook: Workbook) {
    fun createChunkedSourceAudio(source: File, cues: List<AudioCue>) {
        val temp = File(source.name).apply { createNewFile() }
        val tempCue = File(temp.parent, "${temp.nameWithoutExtension}.cue")

        val accessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            workbook.target.resourceMetadata,
            workbook.target
        )
        try {
            source.copyTo(temp, true)
            val audio = AudioFile(temp)
            audio.metadata.clearMarkers()
            audio.update()
            for (cue in cues) {
                audio.metadata.addCue(cue.location, cue.label)
            }
            audio.update()
            val path = accessor.projectDir
            ResourceContainer.load(path).use {
                it.addFileToContainer(temp, ".apps/orature/source/audio/${temp.name}")
                if (tempCue.exists()) {
                    it.addFileToContainer(tempCue, ".apps/orature/source/audio/${tempCue.name}")
                }
                it.write()
            }
        } finally {
            temp.delete()
            if (tempCue.exists()) {
                tempCue.delete()
            }
        }
    }
}

class ChunkingViewModel() : ViewModel(), IMarkerViewModel {

    val chapterPageViewModel: ChapterPageViewModel by inject()

    val workbookDataStore: WorkbookDataStore by inject()

    val consumeStepColor = SimpleStringProperty(ACTIVE)
    val verbalizeStepColor = SimpleStringProperty(INACTIVE)
    val chunkStepColor = SimpleStringProperty(INACTIVE)

    val chapterTitle get() = workbookDataStore.activeChapterProperty.value?.title ?: ""
    val pageProperty = SimpleObjectProperty(ChunkingWizardPage.CONSUME)
    val titleProperty = SimpleStringProperty("")
    val stepProperty = SimpleStringProperty("")

    val sourceAudio by workbookDataStore.sourceAudioProperty

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()

    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(1)
    override var resumeAfterScroll: Boolean = false

    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)


    private val builder = ObservableWaveformBuilder()
    lateinit var waveform: Observable<Image>


    override var audioController: AudioPlayerController? = null
    override val audioPlayer = SimpleObjectProperty<IAudioPlayer>()
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)

    private val disposeables = mutableListOf<Disposable>()

    lateinit var audio: AudioFile

    init {
        pageProperty.onChange {
            when (it) {
                ChunkingWizardPage.CONSUME -> {
                    consumeStepColor.set(ACTIVE)
                    verbalizeStepColor.set(INACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                ChunkingWizardPage.VERBALIZE -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(ACTIVE)
                    chunkStepColor.set(INACTIVE)
                }
                ChunkingWizardPage.CHUNK -> {
                    consumeStepColor.set(COMPLETE)
                    verbalizeStepColor.set(COMPLETE)
                    chunkStepColor.set(ACTIVE)
                }
            }
        }
    }

    fun onDockConsume(op: () -> Unit) {
        sourceAudio?.file?.let {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            audio = loadAudio(it)
            createWaveformImages(audio)
            initializeAudioController()
            op.invoke()
        }
    }

    fun onDockChunk() {
        loadMarkers(audio)
    }

    fun loadAudio(audioFile: File): AudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = AudioFile(audioFile)
        player.load(audioFile)
        audioPlayer.set(player)
        return audio
    }

    fun loadMarkers(audio: AudioFile) {
        val totalMarkers: Int = 500
        audio.metadata.clearMarkers()
        markerModel = VerseMarkerModel(audio, totalMarkers)
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }

    fun cleanup() {
        builder.cancel()
    }

    fun saveAndQuit() {
        compositeDisposable.clear()
        audioConnectionFactory.clearPlayerConnections()
        audioPlayer.value.close()
        audioController = null

        val wkbk = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        val cues = markers.filter { it.placed }.map { it.toAudioCue() }

        VerseByVerseChunking(directoryProvider, wkbk, chapter.addChunk, chapter.sort)
            .chunkChunkByChunk(wkbk.source.slug, cues, 1)

        pageProperty.set(ChunkingWizardPage.CONSUME)


        ChunkAudioUseCase(directoryProvider, workbookDataStore.workbook)
            .createChunkedSourceAudio(sourceAudio.file, cues)

        disposeables.forEach { it.dispose() }

        runLater {
            workspace.navigateBack()
        }
    }

    private fun initializeAudioController() {
        audioController = AudioPlayerController()
        audioController?.load(audioPlayer.get())
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun createWaveformImages(audio: AudioFile) {
        imageWidthProperty.set(computeImageWidth(SECONDS_ON_SCREEN))

        waveform = builder.build(
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
}
