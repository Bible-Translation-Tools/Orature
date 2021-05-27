package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single

interface IAppPreferencesRepository {
    fun resumeProjectId(): Single<Int>
    fun setResumeProjectId(id: Int): Completable
}
