package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class ChapterReviewViewModel : PeerEditViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var concatenateAudio: ConcatenateAudio
    @Inject
    lateinit var waveFileCreator: IWaveFileCreator

    val chapterTitleProperty = workbookDataStore.activeChapterTitleBinding()

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
                compiled = file
                newChapterTake(file)
            }
            .doOnSuccess {
                logger.info("Chapter ${chapter.sort} compiled successfully.")
                chapter.audio.insertTake(it)
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
                loadTargetAudio(take)
            }
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
}