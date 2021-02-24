package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.ContentTypeDao
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity

class ContentMapper(private val contentTypeDao: ContentTypeDao) {
    fun mapFromEntity(entity: ContentEntity, selectedTake: Take?, end: Int): Content {
        return Content(
            sort = entity.sort,
            labelKey = entity.labelKey,
            start = entity.start,
            end = end,
            selectedTake = selectedTake,
            text = entity.text,
            format = entity.format,
            type = contentTypeDao.fetchForId(entity.type_fk)!!,
            id = entity.id
        )
    }

    fun mapToEntity(obj: Content, collectionFk: Int = 0): ContentEntity {
        return ContentEntity(
            id = obj.id,
            sort = obj.sort,
            labelKey = obj.labelKey,
            start = obj.start,
            collectionFk = collectionFk,
            selectedTakeFk = obj.selectedTake?.id,
            text = obj.text,
            format = obj.format,
            type_fk = contentTypeDao.fetchId(obj.type)
        )
    }
}
