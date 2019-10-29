package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val APP_SPECIFIC_DIR = ".apps/otter"
const val TAKE_DIR = "$APP_SPECIFIC_DIR/takes"
const val SOURCE_DIR = "$APP_SPECIFIC_DIR/source"

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

    private fun makeExportFilename(): String {
        val lang = workbook.target.language.slug
        val project = workbook.target.slug
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$project-$timestamp.zip"
    }
}
