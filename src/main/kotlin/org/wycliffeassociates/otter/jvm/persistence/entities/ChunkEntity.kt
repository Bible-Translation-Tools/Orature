package org.wycliffeassociates.otter.jvm.persistence.entities

data class ChunkEntity(
        var id: Int,
        var sort: Int,
        var labelKey: String,
        var start: Int,
        var collectionFk: Int,
        var selectedTakeFk: Int?
)