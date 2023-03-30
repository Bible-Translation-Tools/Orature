package org.wycliffeassociates.otter.common.domain.project.exporter

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File

interface IProjectExporter {
    fun export(
        outputDirectory: File,
        resourceMetadata: ResourceMetadata,
        workbook: Workbook,
        options: ExportOptions? = null
    ): Single<ExportResult>
}