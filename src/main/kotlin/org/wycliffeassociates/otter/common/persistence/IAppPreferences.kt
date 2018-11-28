package org.wycliffeassociates.otter.common.persistence

// interface to getting user-independent app preferences
interface IAppPreferences {
    fun currentUserId(): Int?
    fun setCurrentUserId(userId: Int)
    fun appInitialized(): Boolean
    fun setAppInitialized(initialized: Boolean)
    fun editorPluginId(): Int?
    fun setEditorPluginId(id: Int)
    fun recorderPluginId(): Int?
    fun setRecorderPluginId(id: Int)
}