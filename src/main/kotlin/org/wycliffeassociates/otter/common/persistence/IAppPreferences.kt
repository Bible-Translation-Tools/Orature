package org.wycliffeassociates.otter.common.persistence

// interface to getting user-independent app preferences
interface IAppPreferences {
    fun getCurrentUserId(): Int?
    fun setCurrentUserId(userId: Int)
    fun getAppInitialized(): Boolean
    fun setAppInitialized(initialized: Boolean)
}