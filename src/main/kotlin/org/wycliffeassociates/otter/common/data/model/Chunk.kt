package org.wycliffeassociates.otter.common.data.model

data class Chunk(
        var sort: Int,
        var labelKey: String,
        var start: Int,
        var end: Int,
        var selectedTake: Take?,
        var id: Int = 0
)