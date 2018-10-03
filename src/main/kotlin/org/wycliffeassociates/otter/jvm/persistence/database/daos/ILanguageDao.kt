package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.LanguageEntity

// Additional convenience queries for languages
interface ILanguageDao : IDao<LanguageEntity> {
    fun insertAll(entities: List<LanguageEntity>): List<Int>
    fun fetchGateway(): List<LanguageEntity>
    fun fetchTargets(): List<LanguageEntity>
    fun fetchBySlug(slug: String): LanguageEntity
}