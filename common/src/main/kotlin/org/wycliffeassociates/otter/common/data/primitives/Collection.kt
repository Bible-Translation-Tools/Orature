package org.wycliffeassociates.otter.common.data.primitives

data class Collection(
    var sort: Int,
    var slug: String,
    var labelKey: String,
    var titleKey: String,
    var resourceContainer: ResourceMetadata?,
    var id: Int = 0
) : CollectionOrContent
