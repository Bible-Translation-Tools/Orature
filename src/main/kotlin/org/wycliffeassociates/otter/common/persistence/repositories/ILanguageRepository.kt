package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Language

interface ILanguageRepository : IRepository<Language> {
    fun getBySlug(slug: String): Single<Language>
    fun getGateway(): Single<List<Language>>
    fun getTargets(): Single<List<Language>>
}