package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import javax.inject.Inject

class AppPreferencesRepository @Inject constructor(
    private val preferences: IAppPreferences
) : IAppPreferencesRepository {

    override fun resumeProjectId(): Single<Int> {
        return preferences.resumeBookId()
    }

    override fun setResumeProjectId(id: Int): Completable {
        return preferences.setResumeBookId(id)
    }

}
