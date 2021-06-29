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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class ResourceLinkEntity(
    var id: Int,
    var resourceContentFk: Int,
    var contentFk: Int?,
    var collectionFk: Int?,
    var dublinCoreFk: Int
)

/** Build a [ContentEntity] [ResourceLinkEntity] with id=0. */
fun resourceLinkEntity(resource: ContentEntity, content: ContentEntity, metadata: ResourceMetadataEntity) =
    ResourceLinkEntity(
        id = 0,
        resourceContentFk = resource.id,
        contentFk = content.id,
        collectionFk = null,
        dublinCoreFk = metadata.id
    )

/** Build a [CollectionEntity] [ResourceLinkEntity] with id=0. */
fun resourceLinkEntity(resource: ContentEntity, collection: CollectionEntity, metadata: ResourceMetadataEntity) =
    ResourceLinkEntity(
        id = 0,
        resourceContentFk = resource.id,
        contentFk = null,
        collectionFk = collection.id,
        dublinCoreFk = metadata.id
    )
