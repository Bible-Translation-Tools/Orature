package org.wycliffeassociates.otter.jvm.persistence

import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.util.prefs.Preferences

class AppPreferencesTest {

    @Test
    fun testIfPutGetCurrentUserIdPutsAndGetsCorrectInfo() {
        val input = 5
        val expected = input

        val appPreferences = AppPreferences
        appPreferences.setCurrentUserId(input)
        val result = appPreferences.currentUserId()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testIfGetCurrentUserIdWhenNoExistingIdReturnsNull() {
        val expected = null

        val appPreferences = AppPreferences
        val result = appPreferences.currentUserId()

        Assert.assertEquals(expected, result)
    }

    @After
    fun tearDown() {
        Preferences.userNodeForPackage(AppPreferences::class.java).clear()
    }
}