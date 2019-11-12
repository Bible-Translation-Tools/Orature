package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.zip.IZipFileWriter
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProjectExporter(
    private val resourceMetadata: ResourceMetadata,
    private val workbook: Workbook,
    private val projectAudioDirectory: File,
    private val directoryProvider: IDirectoryProvider
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun export(directory: File): Single<ExportResult> {
        return Single
            .fromCallable {
                val zipFilename = makeExportFilename()
                val zipFile = directory.resolve(zipFilename)

                initializeResourceContainer(zipFile)

                directoryProvider.newZipFileWriter(zipFile).use { zipWriter ->
                    copyTakeFiles(zipWriter)
                    copySourceResources(zipWriter)
                    writeSelectedTakesFile(zipWriter)
                }

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                log.error("Failed to export in-progress project", it)
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun initializeResourceContainer(zipFile: File) {
        ResourceContainer
            .create(zipFile) {
                val projectPath = "./" + RcConstants.MEDIA_DIR
                manifest = buildManifest(resourceMetadata, workbook, projectPath)
            }
            .use {
                it.write()
            }
    }

    /** Export a copy of the source RCs for the current book and the current project. */
    private fun copySourceResources(zipWriter: IZipFileWriter) {
        sequenceOf(resourceMetadata, workbook.source.resourceMetadata)
            .map(directoryProvider::getSourceContainerDirectory)
            .toSet()
            .forEach { zipWriter.copyDirectory(it, RcConstants.SOURCE_DIR) }
    }

    private fun copyTakeFiles(zipWriter: IZipFileWriter) {
        val selectedChapters = selectedChapterFilePaths()
        zipWriter.copyDirectory(projectAudioDirectory, RcConstants.TAKE_DIR) { !selectedChapters.contains(it) }
        zipWriter.copyDirectory(projectAudioDirectory, RcConstants.MEDIA_DIR) { selectedChapters.contains(it) }
    }

    private fun writeSelectedTakesFile(zipWriter: IZipFileWriter) {
        zipWriter.bufferedWriter(RcConstants.SELECTED_TAKES_FILE).use { fileWriter ->
            fetchSelectedTakes()
                .map(::relativeTakePath)
                .blockingSubscribe {
                    fileWriter.appendln(it)
                }
        }
    }

    private fun makeExportFilename(): String {
        val lang = workbook.target.language.slug
        val project = workbook.target.slug
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$project-$timestamp.zip"
    }

    private fun fetchSelectedTakes(chaptersOnly: Boolean = false): Observable<Take> {
        val chapters = workbook.target.chapters
        val bookElements = when {
            chaptersOnly -> chapters.cast()
            else -> chapters.concatMap { chapter -> chapter.children.startWith(chapter) }
        }
        return bookElements.mapNotNull { chapterOrChunk -> chapterOrChunk.audio.selected.value?.value }
    }

    private fun selectedChapterFilePaths(): Set<String> {
        return fetchSelectedTakes(chaptersOnly = true)
            .map(this::relativeTakePath)
            .collectInto(hashSetOf<String>(), { set, path -> set.add(path) })
            .blockingGet()
    }

    private fun relativeTakePath(take: Take): String {
        val relativeFile = take.file.relativeToOrSelf(projectAudioDirectory)
        return relativeFile.invariantSeparatorsPath
    }
}
