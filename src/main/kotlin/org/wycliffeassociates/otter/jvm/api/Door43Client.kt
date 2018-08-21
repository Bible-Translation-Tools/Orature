package org.wycliffeassociates.otter.jvm.api

import io.reactivex.Observable

class Door43Client(val retrofitConfig: RetrofitConfig = RetrofitConfig()) {
    fun getAllLanguages(): Observable<List<Door43Language>> = retrofitConfig.getLangs()
}