import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

@RunWith(PowerMockRunner::class)
@PrepareForTest(DirectoryProvider::class)
class TestDirectoryProvider {
    var mockFileSystem = Mockito.mock(FileSystem::class.java)

    val APPDATA_TESTS_TABLE = listOf(
            mapOf(
                    "expected" to "/Users/edvin/Library/Application Support/translationRecorder/database",
                    "os" to "Mac OS X",
                    "separator" to "/",
                    "appdata" to "/Users/edvin"
            ),
            mapOf(
                    "expected" to "/home/edvin/.config/translationRecorder/database",
                    "os" to "Linux",
                    "separator" to "/",
                    "appdata" to "/home/edvin"
            ),
            mapOf(
                    "expected" to "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\database",
                    "os" to "Windows 10",
                    "separator" to "\\",
                    "appdata" to "C:\\Users\\Edvin\\AppData\\Roaming"
            )
    )

    val USERDATA_TESTS_TABLE = listOf(
            mapOf(
                    "expected" to "/Users/edvin/translationRecorder/Projects",
                    "os" to "Mac OS X",
                    "separator" to "/",
                    "home" to "/Users/edvin"
            ),
            mapOf(
                    "expected" to "/home/edvin/translationRecorder/Projects",
                    "os" to "Linux",
                    "separator" to "/",
                    "home" to "/home/edvin"
            ),
            mapOf(
                    "expected" to "C:\\Users\\Edvin\\translationRecorder\\Projects",
                    "os" to "Windows 10",
                    "separator" to "\\",
                    "home" to "C:\\Users\\Edvin"
            )
    )

    val USERIMAGE_TESTS_TABLE = listOf(
            mapOf(
                    "expected" to "/Users/edvin/Library/Application Support/translationRecorder/users/images",
                    "os" to "Mac OS X",
                    "separator" to "/",
                    "appdata" to "/Users/edvin"
            ),
            mapOf(
                    "expected" to "/home/edvin/.config/translationRecorder/users/images",
                    "os" to "Linux",
                    "separator" to "/",
                    "appdata" to "/home/edvin"
            ),
            mapOf(
                    "expected" to "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\users\\images",
                    "os" to "Windows 10",
                    "separator" to "\\",
                    "appdata" to "C:\\Users\\Edvin\\AppData\\Roaming"
            )
    )

    val USERAUDIO_TESTS_TABLE = listOf(
            mapOf(
                    "expected" to "/Users/edvin/Library/Application Support/translationRecorder/users/audio",
                    "os" to "Mac OS X",
                    "separator" to "/",
                    "appdata" to "/Users/edvin"
            ),
            mapOf(
                    "expected" to "/home/edvin/.config/translationRecorder/users/audio",
                    "os" to "Linux",
                    "separator" to "/",
                    "appdata" to "/home/edvin"
            ),
            mapOf(
                    "expected" to "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\users\\audio",
                    "os" to "Windows 10",
                    "separator" to "\\",
                    "appdata" to "C:\\Users\\Edvin\\AppData\\Roaming"
            )
    )

    @Before
    fun setup() {
        // setup up the mock of System
        PowerMockito.mockStatic(System::class.java)
        PowerMockito.mockStatic(FileSystems::class.java)
        Mockito.`when`(FileSystems.getDefault()).thenReturn(mockFileSystem)
        PowerMockito.mockStatic(Files::class.java)
    }

    @Test
    fun testIfCorrectAppDataDirectoryIsReturnedForEachPlatform() {
        for (testCase in APPDATA_TESTS_TABLE) {
            // define the expected result
            val expected = File(testCase["expected"])

            // configure for OS responses
            Mockito.`when`(System.getProperty("os.name")).thenReturn(testCase["os"])
            BDDMockito.`when`(mockFileSystem.separator).thenReturn(testCase["separator"])
            when (testCase["os"]) {
                "Windows 10" -> Mockito.`when`(System.getenv("APPDATA")).thenReturn(testCase["appdata"])
                else -> Mockito.`when`(System.getProperty("user.home")).thenReturn(testCase["appdata"])
            }

            // get the result
            val fileResult = DirectoryProvider("translationRecorder")
                    .getAppDataDirectory("database")

            // assert
            try {
                assertEquals(expected, fileResult)
            } catch (e: AssertionError) {
                // failed the assert
                println("Input OS: ${testCase["os"]}")
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
            // configure for OS responses
            Mockito.`when`(System.getProperty("os.name")).thenReturn(testCase["os"])
            Mockito.`when`(mockFileSystem.separator).thenReturn(testCase["separator"])
            Mockito.`when`(System.getProperty("user.home")).thenReturn(testCase["home"])

            // get the result
            val fileResult = DirectoryProvider("translationRecorder")
                    .getUserDataDirectory("Projects")

            // assert
            try {
                assertEquals(File(testCase["expected"]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase["os"]}")
                println("Expected: ${testCase["expected"]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

    @Test
    fun testIfCorrectUserProfileImageDirectoryIsReturnedForEachPlatform() {
        for (testCase in USERIMAGE_TESTS_TABLE) {
            // configure for OS responses
            Mockito.`when`(System.getProperty("os.name")).thenReturn(testCase["os"])
            BDDMockito.`when`(mockFileSystem.separator).thenReturn(testCase["separator"])
            when (testCase["os"]) {
                "Windows 10" -> Mockito.`when`(System.getenv("APPDATA")).thenReturn(testCase["appdata"])
                else -> Mockito.`when`(System.getProperty("user.home")).thenReturn(testCase["appdata"])
            }

            // get the result
            val fileResult = DirectoryProvider("translationRecorder").userProfileImageDirectory

            // assert
            try {
                assertEquals(File(testCase["expected"]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase["os"]}")
                println("Expected: ${testCase["expected"]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

    @Test
    fun testIfCorrectUserProfileAudioDirectoryIsReturnedForEachPlatform() {
        for (testCase in USERAUDIO_TESTS_TABLE) {
            // configure for OS responses
            Mockito.`when`(System.getProperty("os.name")).thenReturn(testCase["os"])
            BDDMockito.`when`(mockFileSystem.separator).thenReturn(testCase["separator"])
            when (testCase["os"]) {
                "Windows 10" -> Mockito.`when`(System.getenv("APPDATA")).thenReturn(testCase["appdata"])
                else -> Mockito.`when`(System.getProperty("user.home")).thenReturn(testCase["appdata"])
            }

            // get the result
            val fileResult = DirectoryProvider("translationRecorder").userProfileAudioDirectory

            // assert
            try {
                assertEquals(File(testCase["expected"]), fileResult)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase["os"]}")
                println("Expected: ${testCase["expected"]}")
                println("Result:   $fileResult")
                throw e
            }
            if (fileResult.exists()) fileResult.delete()
        }
    }

}