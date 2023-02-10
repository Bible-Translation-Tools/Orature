package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single

interface ProjectImporterCallback {
    fun onRequestUserInput(): Single<ImportOptions>
}