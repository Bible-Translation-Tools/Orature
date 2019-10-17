package org.wycliffeassociates.otter.common.domain.resourcecontainer.export

import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import java.io.File

class ProjectExporter(
    private val resourceContainerRepository: IResourceContainerRepository,
    private val directoryProvider: IDirectoryProvider
) {
    @Suppress("UNUSED_PARAMETER")
    fun export(directory: File): Single<ExportResult> {
        return Single.just(ExportResult.FAILURE)
    }
}
