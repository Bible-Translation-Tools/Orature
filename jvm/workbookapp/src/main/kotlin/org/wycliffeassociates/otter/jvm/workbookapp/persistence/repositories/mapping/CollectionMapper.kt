package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity

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

    fun mapToEntity(obj: Collection, parentFk: Int? = null, sourceFk: Int? = null): CollectionEntity {
        return CollectionEntity(
            obj.id,
            parentFk,
            sourceFk,
            obj.labelKey,
            obj.titleKey,
            obj.slug,
            obj.sort,
            dublinCoreFk = obj.resourceContainer?.id
        )
    }
}