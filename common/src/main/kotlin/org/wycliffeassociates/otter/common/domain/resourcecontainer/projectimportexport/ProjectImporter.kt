package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
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
                importSource(zipFileReader)
                    .map { ImportResult.SUCCESS }
            }
        } catch (e: Exception) {
            Single.just(ImportResult.IMPORT_ERROR)
        }
    }

    private fun importSource(zipFileReader: IZipFileReader): Single<Boolean> {
        return zipFileReader
            .list(RcConstants.SOURCE_DIR)
            .toObservable()
            .filter { it.extension.toLowerCase() == "zip" }
            .flatMapSingle {
                resourceContainerImporter.import(
                    it.nameWithoutExtension,
                    zipFileReader.stream(it.path)
                )
            }
            .contains(ImportResult.SUCCESS)
    }
}
