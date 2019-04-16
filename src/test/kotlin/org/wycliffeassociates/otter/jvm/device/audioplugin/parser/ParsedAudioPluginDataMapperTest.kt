package org.wycliffeassociates.otter.jvm.device.audioplugin.parser

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import java.io.File

@RunWith(PowerMockRunner::class)
@PrepareForTest(ParsedAudioPluginDataMapper::class)
class ParsedAudioPluginDataMapperTest {
    init {
        PowerMockito.mockStatic(System::class.java)
    }

    val PLUGIN_PLATFORM_TABLE = listOf(
            mapOf(
                    "os.name" to "Mac OS X",
                    "expectedExecutable" to "/Applications/Audacity.app/Contents/MacOS/Audacity"
            ),
            mapOf(
                    "os.name" to "Windows 10",
                    "expectedExecutable" to "C:\\Program Files (x86)\\Audacity\\audacity.exe"
            ),
            mapOf(
                    "os.name" to "Linux",
                    "expectedExecutable" to "audacity"
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
                ParsedExecutable(
                        "/Applications/Audacity.app/Contents/MacOS/Audacity",
                        "C:\\Program Files (x86)\\Audacity\\audacity.exe",
                        "audacity"
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
                val result = ParsedAudioPluginDataMapper(osName).mapToAudioPluginData(inputYamlPlugin, inputPluginFile)
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
                ParsedExecutable(
                        "/Applications/Audacity.app/Contents/MacOS/Audacity",
                        "C:\\Program Files (x86)\\Audacity\\audacity.exe",
                        "audacity"
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
                "audacity",
                inputPluginData.args,
                inputPluginFile
        )

        // Run the mapper
        val result = ParsedAudioPluginDataMapper(osName).mapToAudioPluginData(inputPluginData, inputPluginFile)

        Assert.assertEquals(expectedAudioPlugin, result)
    }
}