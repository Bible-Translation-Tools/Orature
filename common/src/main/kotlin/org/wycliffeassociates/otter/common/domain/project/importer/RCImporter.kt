/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.InputStream

/**
 * An importer for Resource Container project format.
 */
abstract class RCImporter(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository
) : IProjectImporter {
    private var next: RCImporter? = null
    private val logger = LoggerFactory.getLogger(javaClass)

    abstract override fun import(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
    ): Single<ImportResult>

    protected fun passToNextImporter(
        file: File,
        callback: ProjectImporterCallback?,
        options: ImportOptions?
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
                import(outFile)
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

    fun isAlreadyImported(file: File): Boolean {
        ResourceContainer.load(file, true).use { rc ->
            val dublinCore = rc.manifest.dublinCore

            return resourceMetadataRepository
                .getAll()
                .map { resources ->
                    resources.any {
                        it.language.slug == dublinCore.language.identifier &&
                                it.identifier == dublinCore.identifier
                    }
                }
                .doOnError {
                    logger.error("Error while checking if RC is already imported.")
                }
                .blockingGet()
        }
    }

    fun setNext(next: RCImporter) {
        this.next = next
    }

    fun getNext() = next
}