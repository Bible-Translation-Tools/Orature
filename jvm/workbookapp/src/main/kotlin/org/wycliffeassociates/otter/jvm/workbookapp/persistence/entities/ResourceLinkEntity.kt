package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class ResourceLinkEntity(
    var id: Int,
    var resourceContentFk: Int,
    var contentFk: Int?,
    var collectionFk: Int?,
    var dublinCoreFk: Int
)