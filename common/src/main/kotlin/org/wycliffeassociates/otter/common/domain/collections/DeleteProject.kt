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
package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeleteProject @Inject constructor(
    private val collectionRepository: ICollectionRepository,
    private val directoryProvider: IDirectoryProvider,
    private val workbookRepository: IWorkbookRepository,
    private val workbookDescriptorRepo: IWorkbookDescriptorRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun delete(workbook: Workbook, deleteFiles: Boolean): Completable {
        // Order matters here, files won't remove anything from the database
        // delete resources will only remove take entries, but needs derived RCs and links intact
        // delete project may remove derived RCs and links, and thus needs to be last
        val targetProject = workbook.target.toCollection()
        return deleteFiles(workbook, deleteFiles)
            .andThen(collectionRepository.deleteResources(targetProject, deleteFiles))
            .andThen(collectionRepository.deleteProject(targetProject, deleteFiles))
    }

    /**
     * Delete the project's content (files & data). This resets the project
     * to its initial state by deleting and re-inserting the project to its group.
     */
    fun delete(workbookDescriptor: WorkbookDescriptor): Completable {
        return Observable
            .fromCallable {
                workbookRepository.get(
                    workbookDescriptor.sourceCollection,
                    workbookDescriptor.targetCollection
                )
            }
            .flatMapCompletable { workbook ->
                delete(workbook, deleteFiles = true)
            }
            .doOnError {
                logger.error("Error while deleting workbook.", it)
            }
            .andThen(
                recreateWorkbookDescriptor(workbookDescriptor)
            )
            .subscribeOn(Schedulers.io())
    }

    /**
     * Deletes all the projects/workbooks including the derived collections & content.
     */
    fun deleteProjects(list: List<WorkbookDescriptor>): Completable {
        return Completable
            .fromAction {
                list.map { workbookRepository.get(it.sourceCollection, it.targetCollection) }
                    .forEach {
                        delete(it, true).blockingAwait() // avoid concurrent accesses to the same file
                    }
            }
            .andThen(workbookDescriptorRepo.delete(list))
            .subscribeOn(Schedulers.single()) // sequential execution of delete to avoid db transaction error
    }

    fun deleteProjectsWithTimer(
        books: List<WorkbookDescriptor>,
        timeoutMillis: Int,
        onBeforeDeleteCallback: () -> Unit = {}
    ): Completable {
        return Completable
            .timer(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .andThen {
                onBeforeDeleteCallback()
                it.onComplete()
            }
            .andThen(deleteProjects(books))
    }

    private fun recreateWorkbookDescriptor(workbookDescriptor: WorkbookDescriptor): Completable {
        val sourceMetadata = workbookDescriptor.sourceCollection.resourceContainer!!
        return collectionRepository
            .deriveProject(
                listOf(sourceMetadata),
                workbookDescriptor.sourceCollection,
                workbookDescriptor.targetLanguage,
                workbookDescriptor.mode != ProjectMode.TRANSLATION,
                workbookDescriptor.mode
            )
            .doOnError {
                logger.error("Error while recreating workbook descriptor.", it)
            }
            .ignoreElement()
    }

    private fun deleteFiles(workbook: Workbook, deleteFiles: Boolean): Completable {
        return if (deleteFiles) {
            Completable.fromCallable {
                val source = workbook.source
                val target = workbook.target

                directoryProvider.getProjectDirectory(
                    source = source.resourceMetadata,
                    target = target.resourceMetadata,
                    bookSlug = target.slug
                )
                    .deleteRecursively()

                // delete linked resources project files
                target.linkedResources.forEach {
                    directoryProvider.getProjectDirectory(
                        source = source.resourceMetadata,
                        target = it,
                        bookSlug = target.slug
                    )
                        .deleteRecursively()
                }
            }
        } else {
            Completable.complete()
        }
    }
}
