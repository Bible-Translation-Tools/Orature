package persistence.mapping

import data.model.Language
import data.mapping.Mapper
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