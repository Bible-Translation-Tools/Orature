package org.wycliffeassociates.otter.common.data.model

data class Language(
        var slug: String,
        var name: String,
        var anglicizedName: String,
        var direction: String,
        var isGateway: Boolean,
        var id: Int = 0
)