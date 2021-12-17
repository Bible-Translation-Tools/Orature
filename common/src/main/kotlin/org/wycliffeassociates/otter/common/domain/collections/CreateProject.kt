/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import javax.inject.Inject

class CreateProject @Inject constructor(
    private val collectionRepo: ICollectionRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository
) {
    /**
     * Create derived collections for each source RC that has content in sourceProject's subtree, optionally
     * limited to resourceId (if not null).
     */
    fun create(
        sourceProject: Collection,
        targetLanguage: Language,
        resourceId: String? = null
    ): Single<Collection> {
        // Find the source RC and its linked (help) RCs
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")
        val sourceLinkedRcs = resourceMetadataRepo.getLinked(sourceRc)
            .toObservable()
            .flatMapIterable()
        val sourceAndLinkedRcs = sourceLinkedRcs.startWith(sourceRc)

        // If a resourceId filter is requested, apply it.
        val matchingRcs = when (resourceId) {
            null -> sourceAndLinkedRcs
            else -> sourceAndLinkedRcs.filter { resourceId == it.identifier }
        }

        // Create derived projects for each of the sources
        return matchingRcs
            .toList()
            .flatMap {
                collectionRepo.deriveProject(it, sourceProject, targetLanguage)
            }
    }
}
