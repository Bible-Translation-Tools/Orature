package org.wycliffeassociates.otter.jvm.workbookapp.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File

class NewSourceImporter : RCImporter() {
    override fun import(
        file: File,
        options: ImportOptions, callback:
        ProjectImporterCallback
    ): Single<ImportResult> {
        TODO("Not yet implemented")
    }
}