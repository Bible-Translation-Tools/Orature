package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class ContentEntity(
    var id: Int,
    var sort: Int,
    var labelKey: String,
    var start: Int,
    var collectionFk: Int,
    var selectedTakeFk: Int?,
    var text: String?,
    var format: String?,
    var type_fk: Int
)
