package org.wycliffeassociates.otter.common.data.model

import java.io.File
import java.util.Calendar

data class Take(
        var filename: String,
        var path: File,
        var number: Int,
        var timestamp: Calendar,
        var isUnheard: Boolean,
        var markers: List<Marker>,
        var id: Int = 0
)