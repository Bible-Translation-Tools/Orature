package org.wycliffeassociates.otter.jvm.persistence.entities

data class ResourceLinkEntity(
        var id: Int,
        var resourceContentFk: Int,
        var contentFk: Int?,
        var collectionFk: Int?
)