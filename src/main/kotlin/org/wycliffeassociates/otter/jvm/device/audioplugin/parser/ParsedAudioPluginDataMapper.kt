package org.wycliffeassociates.otter.jvm.device.audioplugin.parser

import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import java.io.File

class ParsedAudioPluginDataMapper {
    // Map from Jackson parser class to an AudioPlugin
    // No need to map the other way
    fun mapToAudioPluginData(parsedAudioPlugin: ParsedAudioPluginData, sourceFile: File): AudioPluginData {
        val osName = System.getProperty("os.name").toUpperCase()

        // Get the executable for the system we are running on
        val executable = when {
            osName.contains("WIN") -> parsedAudioPlugin.executable.windows
            osName.contains("MAC") -> parsedAudioPlugin.executable.macos
            else -> parsedAudioPlugin.executable.linux
        }

        // Return the audio plugin or throw an UnsupportedPlatformException
        // if no executable was given for this platform
        return AudioPluginData(
                0,
                parsedAudioPlugin.name,
                parsedAudioPlugin.version,
                parsedAudioPlugin.canEdit,
                parsedAudioPlugin.canRecord,
                executable ?: throw UnsupportedPlatformException(),
                parsedAudioPlugin.args,
                sourceFile
        )
    }
}