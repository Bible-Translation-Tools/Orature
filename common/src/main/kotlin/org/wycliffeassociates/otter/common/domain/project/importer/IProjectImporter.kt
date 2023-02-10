package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File

interface IProjectImporter {
    fun import(
        file: File,
        options: ImportOptions,
        callback: ProjectImporterCallback
    ): Single<ImportResult>
}