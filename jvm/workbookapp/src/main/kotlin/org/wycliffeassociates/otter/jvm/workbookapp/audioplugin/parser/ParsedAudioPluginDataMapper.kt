package org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.parser

import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import java.io.File

/** @param osName optionally overrides the value from System.getProperty("os.name") */
class ParsedAudioPluginDataMapper(osName: String? = null) {
    private val osName = (osName ?: System.getProperty("os.name"))
        .toUpperCase()

    /**
     * Map from Jackson parser class to an AudioPlugin. No need to map the other way.
     * @throw UnsupportedPlatformException if no executable was given for this platform
     */
    fun mapToAudioPluginData(parsedAudioPlugin: ParsedAudioPluginData, sourceFile: File): AudioPluginData {
        // Get the executable for the system we are running on
        val executable = when {
            osName.contains("WIN") -> parsedAudioPlugin.executable.windows
            osName.contains("MAC") -> parsedAudioPlugin.executable.macos
            else -> parsedAudioPlugin.executable.linux
        }

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