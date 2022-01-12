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
