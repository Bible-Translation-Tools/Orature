package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.BurritoToResourceContainerConverter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class BurritoImporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val converter: BurritoToResourceContainerConverter,
): IProjectImporter {

    private data class ConversionResult(
        val output: File? = null,
        val result: ImportResult? = null
    )

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private var next: RCImporter? = null

    override fun import(
        burrito: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult> {
        return Single
            .fromCallable {
                callback?.onNotifyProgress(
                    localizeKey = "converting_file",
                    percent = 10.0
                )
                val tempRc = directoryProvider.createTempFile("${burrito.nameWithoutExtension}_converted_rc", ".zip")
                val converted = converter.convert(burrito, tempRc)
                if (!converted || !tempRc.exists()) {
                    val cause = converter.lastConversionError
                    if (cause != null) {
                        logger.error("Burrito conversion failed for {}", burrito.absolutePath, cause)
                    } else {
                        logger.error("Burrito conversion failed for {} with no exception detail", burrito.absolutePath)
                    }
                    return@fromCallable ConversionResult(result = ImportResult.FAILED)
                }
                ConversionResult(output = tempRc)
            }
            .flatMap { conversion ->
                conversion.result?.let { earlyResult ->
                    return@flatMap Single.just(earlyResult)
                }
                next?.import(conversion.output!!, callback, options)
                    ?: Single.just(ImportResult.FAILED)
            }
            .subscribeOn(Schedulers.io())
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }
}
