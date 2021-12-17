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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class ResourceMetadataMapper @Inject constructor() {
    fun mapFromEntity(entity: ResourceMetadataEntity, language: Language): ResourceMetadata {
        return ResourceMetadata(
            entity.conformsTo,
            entity.creator,
            entity.description,
            entity.format,
            entity.identifier,
            LocalDate.parse(entity.issued),
            language,
            LocalDate.parse(entity.modified),
            entity.publisher,
            entity.subject,
            ContainerType.of(entity.type),
            entity.title,
            entity.version,
            entity.license,
            File(entity.path),
            entity.id
        )
    }

    fun mapToEntity(obj: ResourceMetadata, derivedFromFk: Int? = null): ResourceMetadataEntity {
        return ResourceMetadataEntity(
            obj.id,
            obj.conformsTo,
            obj.creator,
            obj.description,
            obj.format,
            obj.identifier,
            obj.issued.toString(),
            obj.language.id,
            obj.modified.toString(),
            obj.publisher,
            obj.subject,
            obj.type.slug,
            obj.title,
            obj.version,
            obj.license,
            obj.path.toURI().path,
            derivedFromFk
        )
    }
}
