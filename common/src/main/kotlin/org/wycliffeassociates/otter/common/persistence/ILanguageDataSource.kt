package org.wycliffeassociates.otter.common.persistence

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.Language

interface ILanguageDataSource {
    fun fetchLanguageNames(url: String): Observable<List<Language>>
}