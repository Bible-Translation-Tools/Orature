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
                TODO("Unimplemented")
            }
            override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
                TODO("Unimplemented")
            }
            override fun onError() {
                TODO("Unimplemented")
            }
        }

        return import(file, callback)
    }
}