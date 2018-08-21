package org.wycliffeassociates.otter.common.data.model

class Book(
        id: Int = 0,
        titleKey: String,
        sort: Int
) : Collection(id, "book", titleKey, sort)
