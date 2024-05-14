package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File
import javax.inject.Inject

class TsImporter @Inject constructor() : IProjectImporter {
    private var next: RCImporter? = null

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        if (!isValidProject(file)) {
            return Single.just(ImportResult.FAILED)
        }
        TODO("Convert ts file to RC")

    }

    private fun isValidProject(file: File): Boolean {
        if (file.isFile) {
            TODO("validate ts file")
            return true
        } else {
            return false
        }
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }

    fun getNext() = next
}