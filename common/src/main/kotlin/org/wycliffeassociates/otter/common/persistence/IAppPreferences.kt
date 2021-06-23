package org.wycliffeassociates.otter.common.persistence

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType

// interface to getting user-independent workbookapp preferences
interface IAppPreferences {
    fun currentUserId(): Single<Int>
    fun setCurrentUserId(userId: Int): Completable
    fun appInitialized(): Single<Boolean>
    fun setAppInitialized(initialized: Boolean): Completable
    fun pluginId(type: PluginType): Single<Int>
    fun setPluginId(type: PluginType, id: Int): Completable
    fun resumeBookId(): Single<Int>
    fun setResumeBookId(id: Int): Completable
    fun lastResource(): Single<String>
    fun setLastResource(resource: String): Completable
}
