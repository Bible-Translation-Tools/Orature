package org.wycliffeassociates.otter.common.persistence

import io.reactivex.Completable
import io.reactivex.Single

// interface to getting user-independent workbookapp preferences
interface IAppPreferences {
    fun currentUserId(): Single<Int>
    fun setCurrentUserId(userId: Int): Completable
    fun appInitialized(): Single<Boolean>
    fun setAppInitialized(initialized: Boolean): Completable
    fun editorPluginId(): Single<Int>
    fun setEditorPluginId(id: Int): Completable
    fun recorderPluginId(): Single<Int>
    fun setRecorderPluginId(id: Int): Completable
    fun markerPluginId(): Single<Int>
    fun setMarkerPluginId(id: Int): Completable
}
