package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.mapping.Mapper
import org.wycliffeassociates.otter.jvm.persistence.entities.LanguageEntity

class LanguageMapper : Mapper<LanguageEntity, Language> {

    override fun mapFromEntity(type: LanguageEntity) =
        Language(
            type.slug,
            type.name,
            type.anglicizedName,
            type.direction.toLowerCase(),
            type.gateway == 1,
            type.id
        )

    override fun mapToEntity(type: Language): LanguageEntity {
        return LanguageEntity(
            type.id,
            type.slug,
            type.name,
            type.anglicizedName,
            type.direction.toLowerCase(),
            if (type.isGateway) 1 else 0
        )
    }

}