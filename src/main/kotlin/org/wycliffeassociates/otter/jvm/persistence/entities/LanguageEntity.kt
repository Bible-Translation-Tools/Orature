package org.wycliffeassociates.otter.jvm.persistence.entities

data class LanguageEntity(
        var id: Int,
        var slug: String,
        var name: String,
        var anglicizedName: String,
        var direction: String,
        var gateway: Int
)