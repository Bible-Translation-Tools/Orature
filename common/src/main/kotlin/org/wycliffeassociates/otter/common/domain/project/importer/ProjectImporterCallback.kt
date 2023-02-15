package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single

interface ProjectImporterCallback {
    fun onRequestUserInput(): Single<ImportOptions>
    fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions>
    fun onError()
}

data class ImportCallbackParameter(val options: List<String>)