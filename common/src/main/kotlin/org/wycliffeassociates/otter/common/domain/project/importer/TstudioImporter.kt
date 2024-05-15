package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.tstudio2rc.Converter
import java.io.File
import javax.inject.Inject

class TstudioImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : IProjectImporter {
    private var next: RCImporter? = null

    override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        return Single
            .fromCallable {
                callback?.onNotifyProgress(
                    localizeKey = "converting_file",
                    percent = 10.0
                )
                Converter().convertToRC(file, directoryProvider.tempDirectory)
            }
            .flatMap { rcFile ->
                next?.import(rcFile, callback, options)
                    ?: Single.just(ImportResult.FAILED)
            }
            .subscribeOn(Schedulers.io())
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }
}