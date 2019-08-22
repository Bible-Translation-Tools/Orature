package org.wycliffeassociates.otter.jvm.persistence.entities

data class MarkerEntity(
    var id: Int,
    var takeFk: Int?,
    var number: Int,
    var position: Int,
    var label: String
)