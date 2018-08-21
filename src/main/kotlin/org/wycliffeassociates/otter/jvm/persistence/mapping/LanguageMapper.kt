package org.wycliffeassociates.otter.jvm.persistence.mapping

import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.mapping.Mapper
import jooq.tables.pojos.LanguageEntity

class LanguageMapper : Mapper<LanguageEntity, Language> {

    override fun mapFromEntity(type: LanguageEntity) =
        Language(
            type.id,
            type.slug,
            type.name,
            type.isgateway == 1,
            type.anglicizedname
        )

    override fun mapToEntity(type: Language): LanguageEntity {
        return LanguageEntity(
            type.id,
            type.slug,
            type.name,
            if (type.isGateway) 1 else 0,
            type.anglicizedName
        )
    }

}