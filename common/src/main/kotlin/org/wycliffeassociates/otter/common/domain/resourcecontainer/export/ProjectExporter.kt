package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import io.reactivex.Observable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.common.utils.mapNotNull
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val APP_SPECIFIC_DIR = ".apps/otter"
const val TAKE_DIR = "$APP_SPECIFIC_DIR/takes"
const val SOURCE_DIR = "$APP_SPECIFIC_DIR/source"
const val SELECTED_TAKES_FILE = "$APP_SPECIFIC_DIR/selected.txt"

class ProjectExporter(
    private val workbook: Workbook,
    private val projectAudioDirectory: File,
    private val resourceRepository: IResourceRepository,
    private val directoryProvider: IDirectoryProvider
) {

    fun export(directory: File): Single<ExportResult> {
        return Single
            .fromCallable {
                val zipFilename = makeExportFilename()
                val zipFile = directory.resolve(zipFilename)

                directoryProvider.newZipFileWriter(zipFile).use { zipWriter ->
                    writeManifest(zipWriter)
                    copyTakeFiles(zipWriter)
                    copySourceResources(zipWriter)
                    writeSelectedTakes(zipWriter)
                }

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                // TODO: log
            }
            .onErrorReturnItem(ExportResult.FAILURE)
    }

    private fun copySourceResources(zipWriter: IZipFileWriter) {
        val sourceMetadata = workbook.source.resourceMetadata
        val sourceDirectory = directoryProvider.getSourceContainerDirectory(sourceMetadata)
        zipWriter.copyDirectory(sourceDirectory, SOURCE_DIR)
    }

    private fun copyTakeFiles(zipWriter: IZipFileWriter) {
        zipWriter.copyDirectory(projectAudioDirectory, TAKE_DIR)
    }

    private fun writeManifest(zipWriter: IZipFileWriter) {
        zipWriter.bufferedWriter("manifest.yaml").use {
            writeManifest(it)
        }
    }

    private fun writeManifest(writer: BufferedWriter) {
        writer.write("TODO")
    }

    private fun writeSelectedTakes(zipWriter: IZipFileWriter) {
        zipWriter.bufferedWriter(SELECTED_TAKES_FILE).use { fileWriter ->
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

    private fun fetchSelectedTakes(): Observable<Take> {
        return workbook.target.chapters
            .concatMap { chapter -> chapter.children.startWith(chapter) }
            .mapNotNull { chapterOrChunk -> chapterOrChunk.audio.selected.value?.value }
    }

    private fun relativeTakePath(take: Take): String {
        val relativeFile = take.file.relativeToOrSelf(projectAudioDirectory)
        return relativeFile.invariantSeparatorsPath
    }
}
