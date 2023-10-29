package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import org.wycliffeassociates.otter.common.domain.model.VerseMarkerModel
import org.wycliffeassociates.otter.common.utils.computeFileChecksum
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class ChapterReviewViewModel : ChunkingViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var concatenateAudio: ConcatenateAudio
    @Inject
    lateinit var waveFileCreator: IWaveFileCreator

    val chapterTitleProperty = workbookDataStore.activeChapterTitleBinding()
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val disposable = CompositeDisposable()

    var slider: Slider? = null
    var cleanUpWaveform: () -> Unit = {}


    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    override fun dock() {
        startAnimationTimer()

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        workbookDataStore.activeChunkProperty.set(null)
        audioDataStore.updateSourceAudio()
        audioDataStore.openSourceAudioPlayer()

        compile()
    }

    override fun undock() {
        pause()
        waveformAudioPlayerProperty.value?.stop()
        audioDataStore.stopPlayers()
        markerModel
            ?.writeMarkers()
            ?.subscribe()
        cleanup()
    }

    fun compile() {
        val chapter = workbookDataStore.chapter
        val takes = chapter.chunks.value
            ?.filter { it.hasSelectedAudio() }
            ?.mapNotNull { it.audio.getSelectedTake()?.file }
            ?: return

        var compiled: File? = null

        // Don't place verse markers if the draft comes from user chunks
        concatenateAudio.execute(takes, includeMarkers = false)
            .flatMap { file ->
                val checksum = computeFileChecksum(file)
                if (checksum != null && chapterContentHasChanged(checksum, chapter)) {
                    Single.just(chapter.audio.getSelectedTake()!!)
                } else {
                    compiled = file
                    newChapterTake(file)
                }
            }
            .doOnSuccess { take ->
                logger.info("Chapter ${chapter.sort} compiled successfully.")
                take.checkingState.accept(
                    TakeCheckingState(CheckingStatus.VERSE, take.checksum())
                )
                chapter.audio.insertTake(take)
            }
            .subscribeOn(Schedulers.io())
            .doOnError { e ->
                logger.error("Error compiling chapter ${chapter.sort}", e)
            }
            .observeOnFx()
            .doFinally {
                compiled?.delete()
            }
            .subscribe { take ->
                // TODO: refactor the code above to model
                loadTargetAudio(take)
            }
    }

    override fun placeMarker() {
        markerModel?.let { markerModel ->
            markerModel.addMarker(waveformAudioPlayerProperty.get().getLocationInFrames())
            markers.setAll(markerModel.markers)
        }
        onUndoableAction()
    }

    override fun deleteMarker(id: Int) {
        markerModel?.let { markerModel ->
            markerModel.deleteMarker(id)
            markers.setAll(markerModel.markers)
        }
        onUndoableAction()
    }

    override fun moveMarker(id: Int, start: Int, end: Int) {
        markerModel?.moveMarker(id, start, end)
        onUndoableAction()
    }

    override fun undoMarker() {
        markerModel?.let { markerModel ->
            markerModel.undo()
            markers.setAll(markerModel.markers)
        }
        val dirty = markerModel?.hasDirtyMarkers() ?: false
        translationViewModel.canUndoProperty.set(dirty)
        translationViewModel.canRedoProperty.set(true)
    }

    override fun redoMarker() {
        markerModel?.let { markerModel ->
            markerModel.redo()
            markers.setAll(markerModel.markers)
        }
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(markerModel?.canRedo() == true)
    }

    private fun loadTargetAudio(take: Take) {
        val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
        audioPlayer.load(take.file)
        audioPlayer.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        waveformAudioPlayerProperty.set(audioPlayer)

        loadAudioController(audioPlayer)

        val audio = OratureAudioFile(take.file)
        loadMarkers(audio)
        createWaveformImages(audio)
        subscribeOnWaveformImages()
    }

    private fun loadAudioController(player: IAudioPlayer) {
        audioController = AudioPlayerController(slider).also { controller ->
            controller.load(player)
            isPlayingProperty.bind(controller.isPlayingProperty)
        }
    }

    private fun loadMarkers(audio: OratureAudioFile) {
        markers.clear()
        val sourceAudio = OratureAudioFile(sourceAudio.file)
        val sourceMarkers = sourceAudio.getMarker<VerseMarker>()

        val verseMarkers = audio.getMarker<VerseMarker>()

        markerModel = VerseMarkerModel(
            audio,
            sourceMarkers.size,
            sourceMarkers.map { it.label }
        )
        markers.setAll(
            verseMarkers.map {
                ChunkMarkerModel(AudioCue(it.location, it.label))
            }
        )
        markers.sortBy { it.frame }
    }

    private fun newChapterTake(file: File): Single<Take> {
        return workbookDataStore.chapter.let { chapter ->
            val namer = getFileNamer(chapter)
            val chapterNumber = namer.formatChapterNumber()
            val chapterAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir
                .resolve(chapterNumber)
                .apply { mkdirs() }

            chapter.audio.getNewTakeNumber()
                .map { takeNumber ->
                    createNewTake(
                        takeNumber,
                        namer.generateName(takeNumber, AudioFileFormat.WAV),
                        chapterAudioDir
                    ).also {
                        file.copyTo(it.file, overwrite = true)
                    }
                }
        }
    }

    private fun createNewTake(
        newTakeNumber: Int,
        filename: String,
        audioDir: File
    ): Take {
        val takeFile = audioDir.resolve(File(filename))
        return Take(
            name = takeFile.name,
            file = takeFile,
            number = newTakeNumber,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )
    }

    private fun getFileNamer(recordable: Recordable): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbookDataStore.workbook,
            chapter = workbookDataStore.chapter,
            chunk = null,
            recordable = recordable,
            rcSlug = workbookDataStore.workbook.sourceMetadataSlug
        )
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }

    private fun chapterContentHasChanged(checksum: String, chapter: Chapter): Boolean {
        return checksum == chapter.audio.getSelectedTake()?.checkingState?.value?.checksum
    }
}
