package org.wycliffeassociates.otter.common.domain.project.importer

interface IProjectImporterFactory {
    fun makeImporter(): IProjectImporter
}