package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File

abstract class RCImporter : IProjectImporter {
    private var next: RCImporter? = null

    override fun import(
        file: File,
        options: ImportOptions,
        callback: ProjectImporterCallback
    ): Single<ImportResult> {
        return next?.import(file, options, callback)
            ?: Single.just(ImportResult.FAILED)
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }
}