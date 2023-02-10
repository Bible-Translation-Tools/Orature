package org.wycliffeassociates.otter.jvm.workbookapp.domain.project.importer

import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporterFactory
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter

class RCImporterFactory : IProjectImporterFactory {

    override fun makeImporter(): RCImporter {
        val importer1 = OngoingProjectImporter()
        val importer2 = ExistingSourceImporter()
        val importer3 = NewSourceImporter()

        importer1.setNext(importer2)
        importer2.setNext(importer3)

        return importer1
    }
}