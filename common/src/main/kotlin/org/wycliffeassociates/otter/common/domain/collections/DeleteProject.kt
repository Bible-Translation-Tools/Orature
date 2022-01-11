/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import javax.inject.Inject

class DeleteProject @Inject constructor(
    private val collectionRepository: ICollectionRepository,
    private val directoryProvider: IDirectoryProvider
) {

    fun delete(workbook: Workbook, deleteFiles: Boolean): Completable {
        // Order matters here, files won't remove anything from the database
        // delete resources will only remove take entries, but needs derived RCs and links in tact
        // delete project may remove derived RCs and links, and thus needs to be last
        val targetProject = workbook.target.toCollection()
        return deleteFiles(workbook, deleteFiles)
            .andThen(collectionRepository.deleteResources(targetProject, deleteFiles))
            .andThen(collectionRepository.deleteProject(targetProject, deleteFiles))
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
