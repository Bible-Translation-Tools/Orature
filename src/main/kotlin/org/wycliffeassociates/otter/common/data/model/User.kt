package org.wycliffeassociates.otter.common.data.model

data class User(
        var id: Int = 0,
        val audioHash: String,
        val audioPath: String,
        val imagePath: String,
        val sourceLanguages: MutableList<Language>,
        val targetLanguages: MutableList<Language>,
        val userPreferences: UserPreferences
)



