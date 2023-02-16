package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File

interface IProjectImporter {
    fun import(
        file: File,
        callback: ProjectImporterCallback,
        options: ImportOptions = ImportOptions()
    ): Single<ImportResult>

    fun import(file: File): Single<ImportResult> {
        val callback = object : ProjectImporterCallback {
            override fun onRequestUserInput(): Single<ImportOptions> {
                throw NotImplementedError("This method has no implementation.")
            }
            override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
                throw NotImplementedError("This method has no implementation.")
            }
            override fun onError() {
                throw NotImplementedError("This method has no implementation.")
            }
        }

        return import(file, callback)
    }
}