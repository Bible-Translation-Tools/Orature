/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.plugin

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.domain.plugins.PluginParameters
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import java.io.File
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory

class AudioPluginTest {

    val connectionFactory = Mockito.mock(AudioConnectionFactory::class.java)
    val parameters = Mockito.mock(PluginParameters::class.java)

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
        false,
        "./my-missing-executable",
        listOf("hello"),
        File("some/fake/file/path.yaml")
    )
    val inputFile = File("somefile.wav")

    @Test
    fun testCompletableFinishesForValidCommand() {
        // Create the plugin
        val audioPlugin = AudioPlugin(connectionFactory, inputAudioPluginData)
        audioPlugin
            .launch(inputFile, parameters)
            .blockingAwait()
        // Test only finishes if completable finishes
    }

    @Test
    fun testExceptionThrownWhenExecutableNotFound() {
        // Create the plugin
        val audioPlugin = AudioPlugin(connectionFactory, missingExecutablePlugin)
        try {
            audioPlugin
                .launch(inputFile, parameters)
                .blockingAwait()
            Assert.fail()
        } catch (e: RuntimeException) {
            // IOException thrown as RuntimeException
        }
    }
}
