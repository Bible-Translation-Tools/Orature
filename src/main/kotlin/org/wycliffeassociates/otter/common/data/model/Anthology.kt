package org.wycliffeassociates.otter.common.data.model

class Anthology(
        id: Int = 0,
        titleKey: String,
        sort: Int
) : Collection(id, "anthology", titleKey, sort)