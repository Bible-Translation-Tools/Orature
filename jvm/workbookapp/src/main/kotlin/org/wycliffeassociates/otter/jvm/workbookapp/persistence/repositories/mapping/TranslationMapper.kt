package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TranslationEntity
import javax.inject.Inject

class TranslationMapper @Inject constructor() {
    fun mapFromEntity(type: TranslationEntity, source: Language, target: Language) =
        Translation(
            source,
            target,
            type.id
        )

    fun mapToEntity(type: Translation): TranslationEntity {
        return TranslationEntity(
            type.id,
            type.source.id,
            type.target.id
        )
    }
}
