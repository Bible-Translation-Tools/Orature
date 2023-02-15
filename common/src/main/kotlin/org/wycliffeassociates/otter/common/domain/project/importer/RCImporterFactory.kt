package org.wycliffeassociates.otter.common.domain.project.importer

import javax.inject.Inject
import javax.inject.Provider

class RCImporterFactory @Inject constructor() : IProjectImporterFactory {

    @Inject lateinit var ongoingProjectImporter: Provider<OngoingProjectImporter>
    @Inject lateinit var existingProjectImporter: Provider<ExistingSourceImporter>
    @Inject lateinit var newSourceImporter: Provider<NewSourceImporter>

    override fun makeImporter(): RCImporter {
        val importer1 = ongoingProjectImporter.get()
        val importer2 = existingProjectImporter.get()
        val importer3 = newSourceImporter.get()

        importer1.setNext(importer2)
        importer2.setNext(importer3)

        return importer1
    }
}