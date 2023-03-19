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

    fun updateContent(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult>

    fun updateCollections(
        rc: ResourceContainer,
        rcTree: OtterTree<CollectionOrContent>,
        languageSlug: String
    ): Single<ImportResult>

    fun removeResourceContainer(
        resourceContainer: ResourceContainer
    ): Single<DeleteResult>
}
