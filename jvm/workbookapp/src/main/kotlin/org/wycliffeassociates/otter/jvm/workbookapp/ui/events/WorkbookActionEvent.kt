/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.events

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import tornadofx.FXEvent
import java.io.File

class WorkbookOpenEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookExportDialogOpenEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookQuickBackupEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookDeleteEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookExportEvent(
    val workbook: WorkbookDescriptor,
    val exportType: ExportType,
    val outputDir: File,
    val chapters: List<Int>?
) : FXEvent()

class WorkbookExportFinishEvent(
    val result: ExportResult,
    val project: Collection,
    val file: File? = null
): FXEvent()