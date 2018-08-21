package org.wycliffeassociates.otter.jvm.api
import retrofit2.http.GET
import io.reactivex.Observable

interface Door43Api {

    @GET("/exports/langnames.json")
    fun getLanguages(): Observable<List<Door43Language>>
}