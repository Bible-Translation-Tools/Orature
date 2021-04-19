package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import javax.inject.Inject

class CollectionMapper @Inject constructor() {
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
