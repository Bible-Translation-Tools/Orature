package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File

/**
 * A generic project importer. All project importers must implement/inherit this interface.
 */
interface IProjectImporter {
    /**
     * Imports the given project.
     *
     * @param file the project file to import.
     * @param callback the callback to communicate with the user.
     * @param options custom data for importer.
     */
    fun import(
        file: File,
        callback: ProjectImporterCallback? = null,
        options: ImportOptions? = null
    ): Single<ImportResult>
}