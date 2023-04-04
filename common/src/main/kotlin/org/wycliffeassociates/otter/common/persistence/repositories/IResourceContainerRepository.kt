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

import io.reactivex.Single
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.resourcecontainer.ResourceContainer

interface IResourceContainerRepository {
    fun importResourceContainer(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult>

    /**
     * Updates the content entities in the database with the content in the rcTree.
     *
     * The content could update the text in the content, or if a verse was bridged.
     *
     * @param rc The resource container of the source being updated
     * @param rcTree The tree with the content to update
     */
    fun updateContent(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>
    ): Single<ImportResult>

    /**
     * Updates the sort and title of collections in the database with the content in the rcTree.
     *
     * The sort and title could have been set to default values due to being preallocated by versification
     * without being available in an earlier version of the source container.
     *
     * This will update collections with a sort of Int.MAX_VALUE and a title of empty string.
     * This affects source collections, as well as all projects derived from the source resource container.
     *
     * @param rc The resource container of the source being updated
     * @param rcTree The tree with the collections to update
     */
    fun updateCollectionTitles(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>
    ): Single<ImportResult>

    fun removeResourceContainer(
        resourceContainer: ResourceContainer
    ): Single<DeleteResult>
}
