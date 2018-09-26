package org.wycliffeassociates.otter.common.data.model

data class Collection(
        var sort: Int,
        var slug: String,
        var labelKey: String,
        var titleKey: String,
        var resourceContainer: ResourceContainer,
        var id: Int = 0
)