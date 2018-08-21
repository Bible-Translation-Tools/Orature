package org.wycliffeassociates.otter.common.data.model

data class UserPreferences(
        var id: Int = 0,
        var sourceLanguage: Language,
        var targetLanguage: Language
)