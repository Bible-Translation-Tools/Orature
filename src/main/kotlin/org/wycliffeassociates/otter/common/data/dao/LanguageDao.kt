package org.wycliffeassociates.otter.common.data.dao

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.model.Language

interface LanguageDao : Dao<Language> {
    fun getBySlug(slug: String): Observable<Language>
}