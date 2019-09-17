package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.lang.AssertionError

// Input is List<String>, expected output is List<HelpResource>
typealias TestCaseForParser = Pair<List<String>, List<HelpResource>>

class ParseMdTest {
    // These test cases are designed to test the creation of the HelpResource data objects
    // (including the branching logic of the parseHelp() function)
    private val testParseCases: List<TestCaseForParser> = listOf(
            listOf(
                    "# Title 1",
                    "",
                    "Body 1",
                    "",
                    "# Title 2",
                    "",
                    "Body 2"
            ) to
            listOf(
                    HelpResource("# Title 1", "Body 1"),
                    HelpResource("# Title 2", "Body 2")
            ),

            listOf(
                    "# Title 1",
                    "",
                    "Body 1",
                    "",
                    "Body 2", // Second line of body text
                    "",
                    "# Title 3",
                    "Body 3", // No space before body text
                    "",
                    "# Title 4", // Heading with no body text
                    "",
                    "# Title 5",
                    "",
                    "Body 5",
                    "# Title 6", // No space before title text
                    "",
                    "Body 6"
            ) to
            listOf(
                    HelpResource("# Title 1", "Body 1" + System.lineSeparator() + "Body 2"),
                    HelpResource("# Title 3", "Body 3"),
                    HelpResource("# Title 4", ""),
                    HelpResource("# Title 5", "Body 5"),
                    HelpResource("# Title 6", "Body 6")
            )
    )

    // Testing title text extraction
    private val testGetTitleTextCases = listOf(
            "# Hello" to "Hello",
            "#  Matthew" to "Matthew",
            "## John said" to "John said",
            "# John said # hello" to "John said # hello",
            "#John said hello" to "John said hello",
            "John said hello" to null,
            "John #said hello" to null
    )

    // Testing title recognition
    private val testIsTitleLineCases = listOf(
            "# Matthew" to true,
            "## Matthew" to true,
            "#Matthew" to true,
            "" to false,
            "Matthew # said hello" to false,
            "# " to false
    )

    private fun checkLineOperatorFunction(input: String, output: Any?, expected: Any?) {
        try {
            assertEquals(expected, output)
        } catch (e: AssertionError) {
            println("Input: $input")
            println("Expected: $expected")
            println("Result: $output")
            throw e
        }
    }

    @Test
    fun testGetTitleText() {
        testGetTitleTextCases.forEach {
            val output = ParseMd.getTitleText(it.first)
            checkLineOperatorFunction(it.first, output, it.second)
        }
    }

    @Test
    fun testIsTitleLine() {
        testIsTitleLineCases.forEach {
            val output = ParseMd.isTitleLine(it.first)
            checkLineOperatorFunction(it.first, output, it.second)
        }
    }

    private fun getBufferedReader(lines: List<String>): BufferedReader {
        val lineSeparator = System.lineSeparator()
        val stream: ByteArrayInputStream = lines.joinToString(lineSeparator).byteInputStream()
        return stream.bufferedReader()
    }

    @Test
    fun testParse() {
        testParseCases.forEach {
            val bufferedReader = getBufferedReader(it.first)
            val helpResourceList = ParseMd.parseHelp(bufferedReader)
            try {
                assertEquals(it.second, helpResourceList)
            } catch (e: AssertionError) {
                println("Input: " + it.first.toString())
                println("Expected: " + it.second.toString())
                println("Result: " + helpResourceList.toString())
                throw e
            }
        }
    }
}