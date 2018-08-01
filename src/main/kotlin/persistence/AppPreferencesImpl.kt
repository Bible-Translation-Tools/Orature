package persistence

import data.persistence.AppPreferences
import java.util.prefs.Preferences

// preferences object that stores user-independent preference data
object AppPreferencesImpl : AppPreferences {
    private val CURRENT_USER_ID_KEY = "currentUserId"
    private val preferences = Preferences.userNodeForPackage(AppPreferencesImpl::class.java)

    override fun getCurrentUserId(): Int? {
        val userId = preferences.getInt(CURRENT_USER_ID_KEY, -1)
        return if (userId < 0) null else userId
    }

    override fun setCurrentUserId(userId: Int) {
        preferences.putInt(CURRENT_USER_ID_KEY, userId)
    }
}