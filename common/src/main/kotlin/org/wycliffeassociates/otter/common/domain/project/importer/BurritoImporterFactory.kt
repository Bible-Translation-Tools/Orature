package org.wycliffeassociates.otter.common.domain.project.importer

import javax.inject.Inject
import javax.inject.Provider

class BurritoImporterFactory @Inject constructor() : IProjectImporterFactory {

    @Inject lateinit var burritoImporter: Provider<BurritoImporter>
    @Inject lateinit var existingProjectImporter: Provider<ExistingSourceImporter>
    @Inject lateinit var newSourceImporter: Provider<NewSourceImporter>

    private val importer: BurritoImporter by lazy {
        // ts file is converted to RC and then passed to source importers
        val importer1 = burritoImporter.get()
        val importer2 = existingProjectImporter.get()
        val importer3 = newSourceImporter.get()

        importer1.setNext(importer2)
        importer2.setNext(importer3)

        importer1
    }

    override fun makeImporter(): IProjectImporter = importer
}