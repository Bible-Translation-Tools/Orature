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
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Collection

interface IContentRepository : IRepository<Content> {
    // Insert for a collection
    fun insertForCollection(content: Content, collection: Collection): Single<Int>
    // Get all the chunks for a collection
    fun getByCollection(collection: Collection): Single<List<Content>>
    // Get the collection meta-chunk
    fun getCollectionMetaContent(collection: Collection): Single<Content>
    // Get sources this content is derived from
    fun getSources(content: Content): Single<List<Content>>
    // Update the sources for a content
    fun updateSources(content: Content, sourceContents: List<Content>): Completable
}
