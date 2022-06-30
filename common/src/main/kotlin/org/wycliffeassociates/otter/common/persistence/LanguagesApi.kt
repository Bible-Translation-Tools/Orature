package org.wycliffeassociates.otter.common.persistence

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.Language
import retrofit2.http.GET
import retrofit2.http.Url

interface LanguagesApi {
    @GET
    fun fetchLanguages(@Url url: String) : Observable<List<Language>>
}