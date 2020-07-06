package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.util.zip.ZipFile
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class OtterZipFileTest {

    private val mockZipFile = Mockito.mock(ZipFile::class.java)
    private val mockParentFile = Mockito.mock(OtterFile::class.java)
    private val sep = "/"
    private val basePath = "dir1/dir2/"

    private val testNameCases = listOf(
        "file.md" to "file.md",
        "test1/file.md" to "file.md",
        "test1/test2/file.md" to "file.md",
        "test1/file/" to "file",
        "test1/file" to "file",
        "test1/test2/file/" to "file",
        "test1/test2/file" to "file"
    )

    private val testNameWithoutExtensionCases = listOf(
        "file.md" to "file",
        "test1/file.md" to "file",
        "test1/test2/file.md" to "file",
        "test1" to "test1",
        "test1/" to "test1",
        "test1/test2" to "test2",
        "test1/test2/" to "test2"
    )

    private val testToRelativeStringCases = listOf(
        "./test1/test2/" to "test1/test2",
        ".test1/test2/" to ".test1/test2",
        "test1/test2/" to "test1/test2",
        "./test1/test2" to "test1/test2",
        ".test1/test2" to ".test1/test2",
        "test1/test2" to "test1/test2",
        "test1/test2/file.md" to "test1/test2/file.md",
        "test1/file.md" to "test1/file.md",
        "file.md" to "file.md"
    )

    @Before
    fun setup() {
        Mockito.`when`(mockParentFile.absolutePath)
            .thenReturn(basePath)
    }

    private fun checkStringResult(absPath: String, output: String, expected: String) {
        try {
            assertEquals(expected, output)
        } catch (e: AssertionError) {
            println("Abs path: $absPath")
            println("Expected: $expected")
            println("Result: $output")
            throw e
        }
    }

    @Test
    fun testNameWithoutExtension() {
        testNameWithoutExtensionCases.forEach {
            val ozf = OtterZipFile("$basePath${it.first}", mockZipFile, sep, null)
            checkStringResult(ozf.absolutePath, ozf.nameWithoutExtension, it.second)
        }
    }

    @Test
    fun testName() {
        testNameCases.forEach {
            val ozf = OtterZipFile("$basePath${it.first}", mockZipFile, sep, null)
            checkStringResult(ozf.absolutePath, ozf.name, it.second)
        }
    }

    @Test
    fun testToRelativeString() {
        testToRelativeStringCases.forEach {
            val ozf = OtterZipFile("$basePath${it.first}", mockZipFile, sep, null)
            checkStringResult(ozf.absolutePath, ozf.toRelativeString(mockParentFile), it.second)
        }
    }
}
