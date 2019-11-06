package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.zip.IZipFileReader
import java.io.File
import java.io.IOException

class ProjectImporter(
    private val resourceContainerImporter: ImportResourceContainer,
    private val directoryProvider: IDirectoryProvider
) {
    fun isInProgress(resourceContainer: File): Boolean {
        return try {
            resourceContainer.isFile &&
                    resourceContainer.extension == "zip" &&
                    directoryProvider.newZipFileReader(resourceContainer).exists(RcConstants.SELECTED_TAKES_FILE)
        } catch (e: IOException) {
            false
        }
    }

    fun importInProgress(resourceContainer: File): Single<ImportResult> {
        return try {
            directoryProvider.newZipFileReader(resourceContainer).use { zipFileReader ->
                importSources(zipFileReader)
                    .toSingleDefault(ImportResult.SUCCESS)
            }
        } catch (e: Exception) {
            Single.just(ImportResult.IMPORT_ERROR)
        }
    }

    private fun importSources(zipFileReader: IZipFileReader): Completable {
        return Completable.fromAction {
            val sourceFiles = zipFileReader
                .list(RcConstants.SOURCE_DIR)
                .filter { it.extension.toLowerCase() == "zip" }

            val firstTry = sourceFiles
                .map { importSource(it, zipFileReader) }
                .toMap()

            // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
            if (firstTry.containsValue(ImportResult.SUCCESS)) {
                firstTry
                    .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                    .forEach { (file, _) -> importSource(file, zipFileReader) }
            }
        }
    }

    private fun importSource(fileInZip: File, zipFileReader: IZipFileReader): Pair<File, ImportResult> {
        val name = fileInZip.nameWithoutExtension
        val result = resourceContainerImporter
            .import(name, zipFileReader.stream(fileInZip.path))
            .blockingGet()
        // TODO: Log.info("Import source resource container $name result $result")
        return fileInZip to result
    }
}
