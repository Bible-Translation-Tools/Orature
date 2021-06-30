/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.system

import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import tornadofx.*

/** Null on success, otherwise localized error text. */
val ImportResult.errorMessage: String?
    get() {
        return when (this) {
            ImportResult.SUCCESS -> null
            ImportResult.INVALID_RC -> FX.messages["importErrorInvalidRc"]
            ImportResult.INVALID_CONTENT -> FX.messages["importErrorInvalidContent"]
            ImportResult.UNSUPPORTED_CONTENT -> FX.messages["importErrorUnsupportedContent"]
            ImportResult.IMPORT_ERROR -> FX.messages["importErrorImportError"]
            ImportResult.LOAD_RC_ERROR -> FX.messages["importErrorLoadRcError"]
            ImportResult.ALREADY_EXISTS -> FX.messages["importErrorAlreadyExists"]
            ImportResult.UNMATCHED_HELP -> FX.messages["importErrorUnmatchedHelp"]
        }
    }

/** Null on success, otherwise localized error text. */
val ExportResult.errorMessage: String?
    get() {
        return when (this) {
            ExportResult.SUCCESS -> null
            ExportResult.FAILURE -> FX.messages["exportError"]
        }
    }
