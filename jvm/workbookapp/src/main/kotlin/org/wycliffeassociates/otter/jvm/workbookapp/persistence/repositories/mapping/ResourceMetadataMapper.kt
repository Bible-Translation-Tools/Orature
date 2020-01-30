package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity
import java.io.File
import java.time.LocalDate

class ResourceMetadataMapper {
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
            obj.path.toURI().path,
            derivedFromFk
        )
    }
}