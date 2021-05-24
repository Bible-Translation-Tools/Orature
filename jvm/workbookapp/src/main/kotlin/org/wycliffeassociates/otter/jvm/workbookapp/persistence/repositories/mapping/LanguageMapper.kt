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
            type.direction.toLowerCase(),
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
            type.direction.toLowerCase(),
            if (type.isGateway) 1 else 0,
            type.region
        )
    }
}
