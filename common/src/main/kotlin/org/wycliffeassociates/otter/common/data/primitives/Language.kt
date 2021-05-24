package org.wycliffeassociates.otter.common.data.primitives

data class Language(
    var slug: String,
    var name: String,
    var anglicizedName: String,
    var direction: String,
    var isGateway: Boolean,
    var region: String,
    var id: Int = 0
)
