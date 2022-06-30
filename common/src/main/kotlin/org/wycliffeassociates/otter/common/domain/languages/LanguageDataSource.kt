package org.wycliffeassociates.otter.common.domain.languages

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.ILanguageDataSource
import org.wycliffeassociates.otter.common.persistence.LanguagesApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Inject

class LanguageDataSource @Inject constructor() : ILanguageDataSource {
    override fun fetchLanguageNames(url: String): Observable<List<Language>> {
        // Using localhost as a base url is a workaround, because retrofit always requires base url to be set,
        // even for full dynamic urls like in this case.
        // When retrofit sees that base url and target url are different (scheme, domain),
        // it will use the latter

        val request = Retrofit.Builder()
            .baseUrl("http://localhost")
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(LanguagesApi::class.java)

        return request.fetchLanguages(url)
    }
}