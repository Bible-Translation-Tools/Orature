package org.wycliffeassociates.otter.common.data.persistence

// interface to getting user-independent app preferences
interface IAppPreferences {
    fun getCurrentUserId(): Int?
    fun setCurrentUserId(userId: Int)
}