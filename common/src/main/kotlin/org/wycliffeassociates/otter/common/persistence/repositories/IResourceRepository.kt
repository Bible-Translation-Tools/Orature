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
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

interface IResourceRepository : IRepository<Content> {
    fun getResources(collection: Collection, resourceMetadata: ResourceMetadata): Observable<Content>
    fun getResources(content: Content, resourceMetadata: ResourceMetadata): Observable<Content>
    fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun getResourceMetadata(content: Content): List<ResourceMetadata>
    fun getResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun linkToContent(resource: Content, content: Content, dublinCoreFk: Int): Completable
    fun linkToCollection(resource: Content, collection: Collection, dublinCoreFk: Int): Completable

    // Prepare SubtreeHasResources table
    fun calculateAndSetSubtreeHasResources(collectionId: Int)
}
