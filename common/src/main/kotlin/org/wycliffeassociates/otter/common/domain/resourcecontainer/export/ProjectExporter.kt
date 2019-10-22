package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val APP_SPECIFIC_DIR = ".apps/otter"
const val TAKE_DIR = "$APP_SPECIFIC_DIR/takes"
const val TIMESTAMP_PATTERN = "yyyyMMdd-HHmm"

class ProjectExporter(
    private val sourceMetaData: ResourceMetadata,
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
                    copyTakeFiles(projectAudioDirectory, zipWriter)
                }

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                // TODO: log
            }
            .onErrorReturnItem(ExportResult.FAILURE)
    }

    private fun writeManifest(zipWriter: IZipFileWriter) {
        zipWriter.bufferedWriter("manifest.yaml").use {
            writeManifest(it)
        }
    }

    private fun writeManifest(writer: BufferedWriter) {
        writer.write("TODO")
    }

    private fun copyTakeFiles(projectAudioDirectory: File, zipWriter: IZipFileWriter) {
        zipWriter.copyDirectory(projectAudioDirectory, TAKE_DIR)
    }

    private fun makeExportFilename() = "in-progress-" + timestampForFilename() + ".zip"

    private fun timestampForFilename() = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN))
}
