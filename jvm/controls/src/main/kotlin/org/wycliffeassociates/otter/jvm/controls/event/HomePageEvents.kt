/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import tornadofx.FXEvent
import java.io.File

class LanguageSelectedEvent(val item: Language) : FXEvent()

class ProjectImportFinishEvent(
    val result: ImportResult,
    val project: String? = null,
    val language: String? = null,
    val filePath: String? = null,
    val workbookDescriptor: WorkbookDescriptor? = null,
) : FXEvent()

class ProjectImportEvent(
    val file: File,
) : FXEvent()
