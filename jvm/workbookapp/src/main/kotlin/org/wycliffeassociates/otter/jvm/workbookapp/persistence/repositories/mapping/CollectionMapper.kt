package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import javax.inject.Inject

class CollectionMapper @Inject constructor() {
    fun mapFromEntity(entity: CollectionEntity, metadata: ResourceMetadata?): Collection {
        val chunked = entity.chunked?.let { it == 1 } ?: null
        return Collection(
            entity.sort,
            entity.slug,
            entity.label,
            entity.title,
            metadata,
            chunked,
            entity.id
        )
    }

    fun mapToEntity(obj: Collection, parentFk: Int? = null, sourceFk: Int? = null): CollectionEntity {
        val chunked = obj.chunked?.let { if(it) 1 else 0 } ?: null
        return CollectionEntity(
            obj.id,
            parentFk,
            sourceFk,
            chunked,
            obj.labelKey,
            obj.titleKey,
            obj.slug,
            obj.sort,
            dublinCoreFk = obj.resourceContainer?.id
        )
    }
}
