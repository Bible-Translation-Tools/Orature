package org.wycliffeassociates.otter.common.domain.project.exporter

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.ProjectMetadata
import java.io.File

interface ProjectExporter {
    fun export(
        outputDirectory: File,
        projectMetadata: ProjectMetadata,
        workbook: Workbook,
        options: ExportOptions? = null
    ): Single<ExportResult>
}