package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.InputStream

abstract class RCImporter(
    private val directoryProvider: IDirectoryProvider
) : IProjectImporter {
    private var next: RCImporter? = null

    private val logger = LoggerFactory.getLogger(javaClass)
    private val defaultCallback = object : ProjectImporterCallback {
        override fun onRequestUserInput(): Single<ImportOptions> {
            TODO("Not yet implemented")
        }
        override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
            TODO("Not yet implemented")
        }
        override fun onError() {}
    }

    abstract override fun import(
        file: File,
        callback: ProjectImporterCallback,
        options: ImportOptions
    ): Single<ImportResult>

    protected fun passToNextImporter(
        file: File,
        callback: ProjectImporterCallback,
        options: ImportOptions
    ): Single<ImportResult> {
        return next?.import(file, callback, options)
            ?: Single.just(ImportResult.FAILED)
    }

    protected fun importAsStream(filename: String, stream: InputStream): Single<ImportResult> {
        val outFile = directoryProvider.createTempFile(filename, ".zip")

        return Single
            .fromCallable {
                stream.transferTo(outFile.outputStream())
            }
            .flatMap {
                import(outFile, defaultCallback)
            }
            .doOnError { e ->
                logger.error("Error in import, filename: $filename", e)
            }
            .doFinally {
                stream.close()
                outFile.parentFile.deleteRecursively()
            }
            .subscribeOn(Schedulers.io())
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }

    fun getNext() = next
}