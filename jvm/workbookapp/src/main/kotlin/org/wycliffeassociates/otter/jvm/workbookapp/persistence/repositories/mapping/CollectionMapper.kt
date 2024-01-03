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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import java.time.LocalDateTime
import javax.inject.Inject

class CollectionMapper @Inject constructor() {
    fun mapFromEntity(entity: CollectionEntity, metadata: ResourceMetadata?): Collection {
        return Collection(
            sort = entity.sort,
            slug = entity.slug,
            labelKey = entity.label,
            titleKey = entity.title,
            resourceContainer = metadata,
            modifiedTs = entity.modifiedTs?.let(LocalDateTime::parse),
            id = entity.id
        )
    }

    fun mapToEntity(obj: Collection, parentFk: Int? = null, sourceFk: Int? = null): CollectionEntity {
        return CollectionEntity(
            id = obj.id,
            parentFk = parentFk,
            sourceFk = sourceFk,
            label = obj.labelKey,
            title = obj.titleKey,
            slug = obj.slug,
            sort = obj.sort,
            dublinCoreFk = obj.resourceContainer?.id,
            modifiedTs = obj.modifiedTs?.toString()
        )
    }
}
