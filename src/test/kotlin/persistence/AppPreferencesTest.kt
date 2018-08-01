package persistence

import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.util.prefs.Preferences

class AppPreferencesTest {

    @Test
    fun testIfPutGetCurrentUserIdPutsAndGetsCorrectInfo() {
        val input = 5
        val expected = input

        val appPreferences = AppPreferencesImpl
        appPreferences.setCurrentUserId(input)
        val result = appPreferences.getCurrentUserId()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testIfGetCurrentUserIdWhenNoExistingIdReturnsNull() {
        val expected = null

        val appPreferences = AppPreferencesImpl
        val result = appPreferences.getCurrentUserId()

        Assert.assertEquals(expected, result)
    }

    @After
    fun tearDown() {
        Preferences.userNodeForPackage(AppPreferencesImpl::class.java).clear()
    }
}