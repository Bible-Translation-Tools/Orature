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
package org.wycliffeassociates.otter.jvm.workbookapp.plugin.parser

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import java.io.File

class ParsedAudioPluginDataMapperTest {

    var win = File.createTempFile("windows", ".exe").apply {
        setExecutable(true)
        deleteOnExit()
    }
    var linux = File.createTempFile("linux", ".exe").apply {
        setExecutable(true)
        deleteOnExit()
    }
    var mac = File.createTempFile("mac", ".exe").apply {
        setExecutable(true)
        deleteOnExit()
    }

    val PLUGIN_PLATFORM_TABLE = listOf(
        mapOf(
            "os.name" to "Mac OS X",
            "expectedExecutable" to mac.absolutePath
        ),
        mapOf(
            "os.name" to "Windows 10",
            "expectedExecutable" to win.absolutePath
        ),
        mapOf(
            "os.name" to "Linux",
            "expectedExecutable" to linux.absolutePath
        )
    )

    @Test
    fun testCorrectPluginCreatedForEachPlatform() {
        // Create the inputs for the test
        val inputPluginFile = File("/location/of/plugin/audacity.yaml")
        val inputParsedPlugin = ParsedAudioPluginData(
            "Audacity",
            "1.0.1",
            true,
            false,
            false,
            ParsedExecutable(
                listOf(mac.absolutePath),
                listOf(win.absolutePath),
                listOf(linux.absolutePath)
            ),
            listOf("-t value")
        )

        // Iterate over OS tests
        for (testCase in PLUGIN_PLATFORM_TABLE) {
            // Inject the OS
            val osName = testCase["os.name"]

            // Build the expected result
            val expectedAudioPlugin = AudioPluginData(
                0,
                inputParsedPlugin.name,
                inputParsedPlugin.version,
                inputParsedPlugin.canEdit,
                inputParsedPlugin.canRecord,
                inputParsedPlugin.canMark,
                testCase["expectedExecutable"] ?: "",
                inputParsedPlugin.args,
                inputPluginFile
            )

            // Run the mapper
            val result = ParsedAudioPluginDataMapper(osName).mapToAudioPluginData(inputParsedPlugin, inputPluginFile)

            // Assert the result
            Assert.assertEquals(expectedAudioPlugin, result)
        }
    }

    @Test
    fun testExceptionThrownWhenPlatformNotSupported() {
        // Create the inputs for the test
        val inputPluginFile = File("/location/of/plugin/audacity.yaml")
        // Null executables since the platforms are not supported
        val inputYamlPlugin = ParsedAudioPluginData(
            "Audacity",
            "1.0.1",
            true,
            false,
            false,
            ParsedExecutable(
                null,
                null,
                null
            ),
            listOf("-t value")
        )

        // Iterate over OS tests
        for (testCase in PLUGIN_PLATFORM_TABLE) {
            // Inject the OS
            val osName = testCase["os.name"]

            // Run the mapper
            try {
                ParsedAudioPluginDataMapper(osName).mapToAudioPluginData(inputYamlPlugin, inputPluginFile)
                // Exception should be thrown before this line
                Assert.fail("'${testCase["os.name"]}' case did not thrown unsupported platform exception")
            } catch (e: UnsupportedPlatformException) {
                // Everything okay
            }
        }
    }

    @Test
    fun testUnrecognizedPlatformDefaultsToLinux() {
        // Create the inputs for the test
        val inputPluginFile = File("/location/of/plugin/unrecognizedplatform.yaml")
        val inputPluginData = ParsedAudioPluginData(
            "Audacity",
            "1.0.1",
            true,
            false,
            false,
            ParsedExecutable(
                listOf(mac.absolutePath),
                listOf(win.absolutePath),
                listOf(linux.absolutePath)
            ),
            listOf("-t value")
        )

        // Inject the OS name
        val osName = "HAL/S"

        // Build the expected result
        val expectedAudioPlugin = AudioPluginData(
            0,
            inputPluginData.name,
            inputPluginData.version,
            inputPluginData.canEdit,
            inputPluginData.canRecord,
            inputPluginData.canMark,
            linux.absolutePath,
            inputPluginData.args,
            inputPluginFile
        )

        // Run the mapper
        val result = ParsedAudioPluginDataMapper(osName).mapToAudioPluginData(inputPluginData, inputPluginFile)

        Assert.assertEquals(expectedAudioPlugin, result)
    }
}
