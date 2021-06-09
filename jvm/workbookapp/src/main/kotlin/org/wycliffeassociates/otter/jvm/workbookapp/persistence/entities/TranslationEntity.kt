package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class TranslationEntity(
    var id: Int,
    var sourceFk: Int,
    var targetFk: Int
)
