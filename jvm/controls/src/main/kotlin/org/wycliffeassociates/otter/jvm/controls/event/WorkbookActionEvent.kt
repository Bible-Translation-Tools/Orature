package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import tornadofx.FXEvent
import java.io.File

class WorkbookOpenEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookExportDialogOpenEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookDeleteEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookExportEvent(
    val workbook: WorkbookDescriptor,
    val exportType: ExportType,
    val outputDir: File,
    val chapters: List<Int>
) : FXEvent()

class WorkbookExportFinishEvent(
    val result: ExportResult,
    val project: Collection,
    val file: File? = null
): FXEvent()