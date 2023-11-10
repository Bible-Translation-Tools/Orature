package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.utils.computeFileChecksum
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class ChapterTranslationBuilder @Inject constructor(
    private val concatenateAudio: ConcatenateAudio
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Compiles all chunks' takes into a single candidate take and compares it with
     * the currently selected take (chapter). If they are matching, the selected take
     * will be returned. Otherwise, the candidate take will be selected and returned.
     */
    fun getOrCompile(workbook: Workbook, chapter: Chapter) : Single<Take> {
        val takes = chapter.chunks.value
            ?.filter { it.hasSelectedAudio() }
            ?.mapNotNull { it.audio.getSelectedTake()?.file }
            ?: return Single.error(IllegalStateException("No takes to compile."))

        var compiled: File? = null

        return concatenateAudio.execute(takes, includeMarkers = false)
            .flatMap { file ->
                compiled = file

                val checksum = computeFileChecksum(file)
                if (checksum != null && !needNewChapterTake(checksum, chapter)) {
                    Single.just(
                        chapter.audio.getSelectedTake()!!
                    )
                } else {
                    newChapterTake(workbook, chapter, file)
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
            .doFinally {
                compiled?.delete()
            }
    }

    private fun newChapterTake(
        workbook: Workbook,
        chapter: Chapter,
        file: File
    ): Single<Take> {
        val namer = getFileNamer(workbook, chapter)
        val chapterNumber = namer.formatChapterNumber()
        val chapterAudioDir = workbook.projectFilesAccessor.audioDir
            .resolve(chapterNumber)
            .apply { mkdirs() }

        return chapter.audio.getNewTakeNumber()
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

    private fun getFileNamer(
        workbook: Workbook,
        chapter: Chapter,
    ): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbook,
            chapter = chapter,
            chunk = null,
            recordable = chapter,
            rcSlug = workbook.sourceMetadataSlug
        )
    }

    /**
     * Returns whether the existing chapter take (compiled from chunks)
     * has changed since its last update by comparing it with the newly compiled take.
     *
     * @param checksum computed from the newly compiled take
     * @param chapter chapter to be compared with
     */
    private fun needNewChapterTake(checksum: String, chapter: Chapter): Boolean {
        return chapter.audio.getSelectedTake()?.checkingState?.value?.let {
            checksum != it.checksum && it.status != CheckingStatus.VERSE
        } ?: true
    }
}