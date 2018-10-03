package org.wycliffeassociates.otter.jvm.persistence.entities

data class ResourceLinkEntity(
        var id: Int,
        var resourceChunkFk: Int,
        var chunkFk: Int?,
        var collectionFk: Int?
)