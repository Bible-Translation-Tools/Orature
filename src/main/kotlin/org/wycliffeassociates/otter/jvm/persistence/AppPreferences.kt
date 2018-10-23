package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import java.util.prefs.Preferences

// preferences object that stores user-independent preference data
object AppPreferences : IAppPreferences {
    private val CURRENT_USER_ID_KEY = "currentUserId"
    private val APP_INIT_KEY = "appInitialized"
    private val preferences = Preferences.userNodeForPackage(AppPreferences::class.java)

    override fun getCurrentUserId(): Int? {
        val userId = preferences.getInt(CURRENT_USER_ID_KEY, -1)
        return if (userId < 0) null else userId
    }

    override fun setCurrentUserId(userId: Int) {
        preferences.putInt(CURRENT_USER_ID_KEY, userId)
    }

    override fun getAppInitialized(): Boolean {
        return preferences.getBoolean(APP_INIT_KEY, false)
    }

    override fun setAppInitialized(initialized: Boolean) {
        preferences.putBoolean(APP_INIT_KEY, initialized)
    }
}