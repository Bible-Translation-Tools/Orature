package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity

class CollectionMapper {
    fun mapFromEntity(entity: CollectionEntity, metadata: ResourceMetadata?): Collection {
        return Collection(
                entity.sort,
                entity.slug,
                entity.label,
                entity.title,
                metadata,
                entity.id
        )

    }

    fun mapToEntity(obj: Collection): CollectionEntity {
        return CollectionEntity(
                obj.id,
                null, // filled in by dao when needed
                null,
                obj.labelKey,
                obj.titleKey,
                obj.slug,
                obj.sort,
                metadataFk = obj.resourceContainer?.id ?: null
        )
    }

}