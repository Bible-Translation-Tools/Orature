package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class ProjectExporter(
    private val resourceContainerRepository: IResourceContainerRepository,
    private val directoryProvider: IDirectoryProvider
) {
    fun export(directory: File): Single<ExportResult> {
        val zipFileName = "in-progress-" + LocalDate.now().format(ISO_LOCAL_DATE) + ".zip"
        val zipFile = directory.resolve(zipFileName)

        directoryProvider.newZipFileWriter(zipFile).use { zipWriter ->
            zipWriter.bufferedWriter("manifest.yaml").use {
                it.write("hello world")
            }
        }

        return Single.just(ExportResult.FAILURE)
    }
}
