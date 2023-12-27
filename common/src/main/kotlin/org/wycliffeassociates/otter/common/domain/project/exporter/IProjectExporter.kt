package org.wycliffeassociates.otter.common.domain.project.exporter

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File

interface IProjectExporter {
    fun export(
        outputDirectory: File,
        workbook: Workbook,
        callback: ProjectExporterCallback? = null,
        options: ExportOptions? = null
    ): Single<ExportResult>

    fun estimateExportSize(workbook: Workbook, chapterFilter: List<Int>): Long
}