package org.wycliffeassociates.otter.common.data.model

data class Project(
        var id: Int = 0,
        val targetLanguage: Language,
        val sourceLanguage: Language,
        val book: Book
)