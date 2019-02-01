package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.persistence.entities.ContentEntity

class ContentMapper {
    fun mapFromEntity(entity: ContentEntity, selectedTake: Take?, end: Int): Content {
        return Content(
                entity.sort,
                entity.labelKey,
                entity.start,
                end,
                selectedTake,
                entity.text,
                entity.format,
                entity.id
        )
    }

    fun mapToEntity(obj: Content, collectionFk: Int = 0): ContentEntity {
        return ContentEntity(
                obj.id,
                obj.sort,
                obj.labelKey,
                obj.start,
                collectionFk,
                obj.selectedTake?.id,
                obj.text,
                obj.format
        )
    }

}