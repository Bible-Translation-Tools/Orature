package org.wycliffeassociates.otter.common.data.model

import java.io.File
import java.time.LocalDate

data class Take(
    var filename: String,
    val path: File,
    var number: Int,
    var created: LocalDate,
    var deleted: LocalDate?,
    var played: Boolean,
    var markers: List<Marker>,
    var id: Int = 0
)
