package org.wycliffeassociates.otter.common.data.primitives

data class Marker(
    var number: Int,
    var position: Int,
    var label: String,
    var id: Int = 0
)
