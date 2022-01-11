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

import org.wycliffeassociates.otter.common.data.primitives.Marker
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TakeEntity
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class TakeMapper @Inject constructor() {
    fun mapFromEntity(entity: TakeEntity, markers: List<Marker>): Take {
        return Take(
            filename = entity.filename,
            path = File(entity.filepath),
            number = entity.number,
            created = entity.createdTs.let(LocalDate::parse),
            deleted = entity.deletedTs?.let(LocalDate::parse),
            played = entity.played == 1,
            markers = markers,
            id = entity.id
        )
    }

    fun mapToEntity(obj: Take, contentFk: Int = -1): TakeEntity {
        return TakeEntity(
            id = obj.id,
            contentFk = contentFk,
            filename = obj.filename,
            filepath = obj.path.toURI().path,
            number = obj.number,
            createdTs = obj.created.toString(),
            deletedTs = obj.deleted?.toString(),
            played = if (obj.played) 1 else 0
        )
    }
}
