import app.filesystem.DirectoryManager

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

@RunWith(PowerMockRunner::class)
@PrepareForTest(DirectoryManager::class)
class TestDirectoryManager {
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

    @Before
    fun setup() {
        // setup up the mock of System
        PowerMockito.mockStatic(System::class.java)
        PowerMockito.mockStatic(FileSystems::class.java)
        BDDMockito.given(FileSystems.getDefault()).willReturn(mockFileSystem)
        PowerMockito.mockStatic(Files::class.java)
    }

    @Test
    fun testIfCorrectAppDataDirectoryPathIsReturnedForEachPlatform() {
        for (testCase in APPDATA_TESTS_TABLE) {
            // define the expected result
            val expected = testCase["expected"]

            // configure for OS responses
            BDDMockito.given(System.getProperty("os.name")).willReturn(testCase["os"])
            BDDMockito.given(mockFileSystem.separator).willReturn(testCase["separator"])
            when (testCase["os"]) {
                "Windows 10" -> BDDMockito.given(System.getenv("APPDATA")).willReturn(testCase["appdata"])
                else -> BDDMockito.given(System.getProperty("user.home")).willReturn(testCase["appdata"])
            }

            // get the result
            val result = DirectoryManager("translationRecorder")
                    .getAppDataDirectory("database", false)

            // assert
            try {
                assertEquals(expected, result)
            } catch (e: AssertionError) {
                // failed the assert
                println("Input OS: ${testCase["os"]}")
                println("Expected: $expected")
                println("Result:   $result")
                throw e
            }
        }
    }

    // Test if file system API is called correctly
    @Test
    fun testIfFileCreateDirectoriesCalledWhenAppDataDirectoryDoesNotExist() {
        // Only Windows 10 is checked since this is not OS dependent code

        // define the expected result
        val expected = "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\database"
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Windows 10 responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getenv("APPDATA")).willReturn("C:\\Users\\Edvin\\AppData\\Roaming")

        // force file to not exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)

        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            it.arguments.first()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testIfFileCreateDirectoriesNotCalledWhenAppDataDirectoryDoesExist() {
        // Only Windows 10 is checked since this is not OS dependent code

        // define the expected result
        val expected = "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\database"
        val expectedDidCallFileSystem = false

        // set default result
        var resultDidCallFileSystem = false

        // configure for Windows 10 responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getenv("APPDATA")).willReturn("C:\\Users\\Edvin\\AppData\\Roaming")

        // force file to exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(false)

        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            it.arguments.first()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testIfEmptyPathIsReturnedWhenExceptionThrownWhileCreatingAppDataDirectory() {
        // Only windows 10 is checked since this is not OS dependent code
        // define the expected result
        val expected = ""
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getenv("APPDATA")).willReturn("C:\\Users\\Edvin\\AppData\\Roaming")

        // force file to not exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)

        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            throw Exception() // throw the exception
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testIfEmptyPathIsReturnedForAppDataDirectoryWhenUnrecognizedOS() {
        // define the expected result
        val expected = ""

        // configure for fake OS responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Android")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testIfCorrectUserDataDirectoryPathIsReturnedForEachPlatform() {
        for (testCase in USERDATA_TESTS_TABLE) {
            // configure for OS responses
            BDDMockito.given(System.getProperty("os.name")).willReturn(testCase["os"])
            BDDMockito.given(mockFileSystem.separator).willReturn(testCase["separator"])
            BDDMockito.given(System.getProperty("user.home")).willReturn(testCase["home"])

            // get the result
            val result = DirectoryManager("translationRecorder")
                    .getUserDataDirectory("Projects", false)

            // assert
            try {
                assertEquals(testCase["expected"], result)
            } catch (e: AssertionError) {
                println("Input OS: ${testCase["os"]}")
                println("Expected: ${testCase["expected"]}")
                println("Result:   $result")
                throw e
            }
        }
    }

    @Test
    fun testIfFileCreateDirectoriesCalledWhenUserDataDirectoryDoesNotExist() {
        // define the expected result
        val expected = "C:\\Users\\Edvin\\translationRecorder\\Projects"
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getProperty("user.home")).willReturn("C:\\Users\\Edvin")

        // force file to not exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)
        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            it.arguments.first()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testIfFileCreateDirectoriesNotCalledWhenUserDataDirectoryDoesExist() {
        // define the expected result
        val expected = "C:\\Users\\Edvin\\translationRecorder\\Projects"
        val expectedDidCallFileSystem = false

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getProperty("user.home")).willReturn("C:\\Users\\Edvin")

        // force file to exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(false)
        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            it.arguments.first()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testIfEmptyPathIsReturnedWhenExceptionThrownWhileCreatingUserDataDirectory() {
        // Only windows 10 is checked since this is not OS dependent code

        // define the expected result
        val expected = ""
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getProperty("user.home")).willReturn("C:\\Users\\Edvin")

        // force file to not exist
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)
        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            throw Exception() // throw the exception
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }
}