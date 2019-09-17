package org.wycliffeassociates.otter.common.domain.resourcecontainer

class ImportException(val result: ImportResult) : Exception()

/** Return an ImportException, either by casting this, or searching cause/suppressed exceptions */
fun Throwable.castOrFindImportException(): ImportException? =
    if (this is ImportException) this
    else listOfNotNull(cause, *suppressed)
        .mapNotNull(Throwable::castOrFindImportException)
        .firstOrNull()
