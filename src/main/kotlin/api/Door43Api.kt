package api
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable

interface Door43Api {

    @GET("/exports/langnames.json")
    fun getLanguages(): Observable<List<Door43Language>>
}