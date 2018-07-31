package data.model

class Book(
        id: Int = 0,
        titleKey: String,
        sort: Int
) : Collection(id, "book", titleKey, sort)
