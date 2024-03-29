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

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.mapping.Mapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.LanguageEntity
import javax.inject.Inject

class LanguageMapper @Inject constructor() : Mapper<LanguageEntity, Language> {

    override fun mapFromEntity(type: LanguageEntity) =
        Language(
            type.slug,
            type.name,
            type.anglicizedName,
            type.direction.lowercase(),
            type.gateway == 1,
            type.region,
            type.id
        )

    override fun mapToEntity(type: Language): LanguageEntity {
        return LanguageEntity(
            type.id,
            type.slug,
            type.name,
            type.anglicizedName,
            type.direction.lowercase(),
            if (type.isGateway) 1 else 0,
            type.region
        )
    }
}
