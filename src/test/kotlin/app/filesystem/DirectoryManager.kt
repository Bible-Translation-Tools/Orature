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

    @Before
    fun setup() {
        // setup up the mock of System
        PowerMockito.mockStatic(System::class.java)
        PowerMockito.mockStatic(FileSystems::class.java)
        BDDMockito.given(FileSystems.getDefault()).willReturn(mockFileSystem)
        PowerMockito.mockStatic(Files::class.java)
    }

    @Test
    fun testIfCorrectAppDataDirectoryPathIsReturnedForMacOSX() {
        // define the expected result
        val expected = "/Users/edvin/Library/Application Support/translationRecorder/database"

        // configure for Mac OS X responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Mac OS X")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/Users/edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testLinuxAppDataDirectory() {
        // define the expected result
        val expected = "/home/edvin/.config/translationRecorder/database"

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testWindows10AppDataDirectory() {
        // define the expected result
        val expected = "C:\\Users\\Edvin\\AppData\\Roaming\\translationRecorder\\database"

        // configure for Windows 10 responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getenv("APPDATA")).willReturn("C:\\Users\\Edvin\\AppData\\Roaming")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", false)

        // assert
        assertEquals(expected, result)
    }

    // Test if file system API is called correctly
    @Test
    fun testAppDataDirectoryCreateIfNotExists() {
        // define the expected result
        val expected = "/home/edvin/.config/translationRecorder/database"
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Windows 10 responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")
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
    fun testAppDataDirectoryCreateIfNotExistsException() {
        // define the expected result
        val expected = ""
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)
        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            throw Exception()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getAppDataDirectory("database", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }

    @Test
    fun testAppDataDirectoryUnrecognizedOS() {
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
    fun testMacOSXUserDataDirectory() {
        // define the expected result
        val expected = "/Users/Edvin/translationRecorder/Projects"

        // configure for Mac OS X responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Mac OS X")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/Users/Edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testLinuxUserDataDirectory() {
        // define the expected result
        val expected = "/home/edvin/translationRecorder/Projects"

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testWindows10UserDataDirectory() {
        // define the expected result
        val expected = "C:\\Users\\Edvin\\translationRecorder\\Projects"

        // configure for Windows 10 responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Windows 10")
        BDDMockito.given(mockFileSystem.separator).willReturn("\\")
        BDDMockito.given(System.getProperty("user.home")).willReturn("C:\\Users\\Edvin")

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", false)

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun testUserDataDirectoryCreateIfNotExists() {
        // define the expected result
        val expected = "/home/edvin/translationRecorder/Projects"
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")
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
    fun testUserDataDirectoryCreateIfNotExistsException() {
        // define the expected result
        val expected = ""
        val expectedDidCallFileSystem = true

        // set default result
        var resultDidCallFileSystem = false

        // configure for Linux responses
        BDDMockito.given(System.getProperty("os.name")).willReturn("Linux")
        BDDMockito.given(mockFileSystem.separator).willReturn("/")
        BDDMockito.given(System.getProperty("user.home")).willReturn("/home/edvin")
        BDDMockito.given(Files.notExists(any(Path::class.java))).willReturn(true)
        BDDMockito.given(Files.createDirectories(any(Path::class.java))).will {
            resultDidCallFileSystem = true
            throw Exception()
        }

        // get the result
        val result = DirectoryManager("translationRecorder")
                .getUserDataDirectory("Projects", true)

        // assert
        assertEquals(expected, result)
        assertEquals(expectedDidCallFileSystem, resultDidCallFileSystem)
    }
}