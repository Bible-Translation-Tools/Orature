package org.wycliffeassociates.otter.common.domain.project.exporter

import io.reactivex.Single
import java.io.File

interface ProjectExporter {
    fun export(
        targetDir: File,
        options: ExportOptions?,
        callback: ProjectExporterCallback?
    ): Single<ExportResult>
}