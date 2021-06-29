/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
import TestCaseParam.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import java.io.File

enum class TestCaseParam {
    EXPECT,
    OS,
    SEPARATOR,
    USERHOME,
    WINAPPDATA
}

typealias TestCase = Map<TestCaseParam, String>

class TestDirectoryProvider {
    private val macParams = mapOf(
        OS to "Mac OS X",
        SEPARATOR to "/",
        USERHOME to "/Users/edvin"
    )

    private val linuxParams = mapOf(
        OS to "Linux",
        SEPARATOR to "/",
        USERHOME to "/home/edvin"
    )

    private val winParams = mapOf(
        OS to "Windows 10",
        SEPARATOR to "\\",
        USERHOME to "C:\\Users\\Edvin",
        WINAPPDATA to "C:\\Users\\Edvin\\AppData\\Roaming"
    )

    val APPDATA_TESTS_TABLE = listOf<TestCase>(
        macParams + (EXPECT to
                "/Users/edvin/Library/Application Support/Orature/database"),
        linuxParams + (EXPECT to
                "/home/edvin/.config/Orature/database"),
        winParams + (EXPECT to
                "C:\\Users\\Edvin\\AppData\\Roaming\\Orature\\database")
    )

    val USERDATA_TESTS_TABLE = listOf<TestCase>(
        macParams + (EXPECT to
                "/Users/edvin/Orature/Projects"),
        linuxParams + (EXPECT to
                "/home/edvin/Orature/Projects"),
        winParams + (EXPECT to
                "C:\\Users\\Edvin\\Orature\\Projects")
    )

    val USERIMAGE_TESTS_TABLE = listOf<TestCase>(
        macParams + (EXPECT to
                "/Users/edvin/Library/Application Support/Orature/users/images"),
        linuxParams + (EXPECT to
                "/home/edvin/.config/Orature/users/images"),
        winParams + (EXPECT to
                "C:\\Users\\Edvin\\AppData\\Roaming\\Orature\\users\\images")
    )

    val USERAUDIO_TESTS_TABLE = listOf<TestCase>(
        macParams + (EXPECT to
                "/Users/edvin/Library/Application Support/Orature/users/audio"),
        linuxParams + (EXPECT to
                "/home/edvin/.config/Orature/users/audio"),
        winParams + (EXPECT to
                "C:\\Users\\Edvin\\AppData\\Roaming\\Orature\\users\\audio")
    )

    private fun buildDirectoryProvider(testCase: TestCase) = DirectoryProvider(
        "Orature",
        pathSeparator = testCase[SEPARATOR],
        userHome = testCase[USERHOME],
        windowsAppData = testCase[WINAPPDATA],
        osName = testCase[OS]
    )

    @Test
    fun testIfCorrectAppDataDirectoryIsReturnedForEachPlatform() {
        for (testCase in APPDATA_TESTS_TABLE) {
            val expected = File(testCase[EXPECT])

            val directoryProvider = buildDirectoryProvider(testCase)

            val fileResult = directoryProvider.getAppDataDirectory("database")

            try {
                assertEquals(expected, fileResult)
            } catch (e: AssertionError) {
                // failed the assert
                println("Input OS: ${testCase[OS]}")
                println("Expected: $expected")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

    @Test
    fun testIfCorrectUserDataDirectoryIsReturnedForEachPlatform() {
        for (testCase in USERDATA_TESTS_TABLE) {
            // get the result
            val fileResult = buildDirectoryProvider(testCase)
                .getUserDataDirectory("Projects")

            // assert
            try {
                assertEquals(File(testCase[EXPECT]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase[OS]}")
                println("Expected: ${testCase[EXPECT]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

    @Test
    fun testIfCorrectUserProfileImageDirectoryIsReturnedForEachPlatform() {
        for (testCase in USERIMAGE_TESTS_TABLE) {
            // get the result
            val fileResult = buildDirectoryProvider(testCase)
                .userProfileImageDirectory

            // assert
            try {
                assertEquals(File(testCase[EXPECT]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase[OS]}")
                println("Expected: ${testCase[EXPECT]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

    @Test
    fun testIfCorrectUserProfileAudioDirectoryIsReturnedForEachPlatform() {
        for (testCase in USERAUDIO_TESTS_TABLE) {
            // get the result
            val fileResult = buildDirectoryProvider(testCase)
                .userProfileAudioDirectory

            // assert
            try {
                assertEquals(File(testCase[EXPECT]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase[OS]}")
                println("Expected: ${testCase[EXPECT]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }
}