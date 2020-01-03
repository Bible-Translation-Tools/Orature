package org.wycliffeassociates.otter.jvm.workbookapp.audioplugin

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import java.io.File

class AudioPluginTest {

    var exe = File.createTempFile("test", ".exe").apply {
        setExecutable(true)
        deleteOnExit()
    }

    var inputAudioPluginData = AudioPluginData(
        0,
        "Beethoven",
        "3.4.2",
        true,
        false,
        "bash",
        listOf("-c", "echo hello"),
        exe
    )
    var missingExecutablePlugin = AudioPluginData(
        0,
        "Beethoven",
        "3.4.2",
        true,
        false,
        "./my-missing-executable",
        listOf("hello"),
        File("some/fake/file/path.yaml")
    )
    val inputFile = File("somefile.wav")

    @Test
    fun testCompletableFinishesForValidCommand() {
        // Create the plugin
        val audioPlugin = AudioPlugin(inputAudioPluginData)
        audioPlugin
            .launch(inputFile)
            .blockingAwait()
        // Test only finishes if completable finishes
    }

    @Test
    fun testExceptionThrownWhenExecutableNotFound() {
        // Create the plugin
        val audioPlugin = AudioPlugin(missingExecutablePlugin)
        try {
            audioPlugin
                .launch(inputFile)
                .blockingAwait()
            Assert.fail()
        } catch (e: RuntimeException) {
            // IOException thrown as RuntimeException
        }
    }
}