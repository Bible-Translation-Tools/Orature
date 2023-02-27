package org.wycliffeassociates.otter.common.domain.project.importer

/**
 * The options provided for importing a project.
 *
 * @param chapters filters which chapter(s) of the project will be imported.
 * @param confirmed user's response to proceed with the action/request. This
 * value is returned from a callback.
 */
data class ImportOptions(
    val chapters: List<Int>? = null,
    val confirmed: Boolean? = null
)