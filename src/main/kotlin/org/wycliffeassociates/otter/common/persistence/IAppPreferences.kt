package org.wycliffeassociates.otter.common.persistence

// interface to getting user-independent app preferences
interface IAppPreferences {
    fun getCurrentUserId(): Int?
    fun setCurrentUserId(userId: Int)
    fun getAppInitialized(): Boolean
    fun setAppInitialized(initialized: Boolean)
    fun getEditorPluginId(): Int?
    fun setEditorPluginId(id: Int)
    fun getRecorderPluginId(): Int?
    fun setRecorderPluginId(id: Int)
}