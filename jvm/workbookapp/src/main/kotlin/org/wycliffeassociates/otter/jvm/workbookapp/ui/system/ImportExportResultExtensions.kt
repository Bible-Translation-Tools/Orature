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
