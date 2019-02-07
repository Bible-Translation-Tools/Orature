package org.wycliffeassociates.otter.common.domain.resourcecontainer

enum class ImportResult {
    SUCCESS,
    INVALID_RC,
    INVALID_CONTENT,
    UNSUPPORTED_CONTENT,
    IMPORT_ERROR,
    LOAD_RC_ERROR,
    ALREADY_EXISTS
}