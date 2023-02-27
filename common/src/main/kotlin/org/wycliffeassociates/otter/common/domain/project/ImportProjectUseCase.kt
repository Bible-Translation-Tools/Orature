package org.wycliffeassociates.otter.common.domain.project

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Provider

class ImportProjectUseCase @Inject constructor() {

    @Inject
    lateinit var rcFactoryProvider: Provider<RCImporterFactory>

    @Throws(
        IllegalArgumentException::class,
        InvalidResourceContainerException::class
    )
    fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions? = null
    ): Single<ImportResult> {
        val format = ProjectFormatIdentifier.getProjectFormat(file)
        val importer = getImporter(format)

        return importer.import(file, callback, options)
    }

    fun import(file: File): Single<ImportResult> {
        return import(file, null, null)
    }

    fun isAlreadyImported(file: File): Boolean {
        return rcFactoryProvider.get()
            .makeImporter()
            .isAlreadyImported(file)
    }

    /**
     * Get the corresponding importer based on the project format.
     */
    private fun getImporter(format: ProjectFormat): IProjectImporter {
        /*
            If we support 2+ formats, uncomment this
            val factory = when (format) { ... }
        */
        val factory: IProjectImporterFactory = rcFactoryProvider.get()
        return factory.makeImporter()
    }
}