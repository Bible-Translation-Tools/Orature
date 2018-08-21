package data.model

class Anthology(
        id: Int = 0,
        titleKey: String,
        sort: Int
) : Collection(id, "anthology", titleKey, sort)