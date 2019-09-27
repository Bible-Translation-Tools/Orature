package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class CollectionEntity(
    var id: Int,
    var parentFk: Int?,
    var sourceFk: Int?,
    var label: String,
    var title: String,
    var slug: String,
    var sort: Int,
    var dublinCoreFk: Int?
)