package org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.parser

import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
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
        val executable = selectExecutable(parsedAudioPlugin)

        return AudioPluginData(
            0,
            parsedAudioPlugin.name,
            parsedAudioPlugin.version,
            parsedAudioPlugin.canEdit,
            parsedAudioPlugin.canRecord,
            parsedAudioPlugin.canMark,
            executable ?: throw UnsupportedPlatformException(),
            parsedAudioPlugin.args,
            sourceFile
        )
    }

    private fun selectExecutable(parsedAudioPlugin: ParsedAudioPluginData): String? {
        val options = when {
            osName.contains("WIN") -> parsedAudioPlugin.executable.windows
            osName.contains("MAC") -> parsedAudioPlugin.executable.macos
            else -> parsedAudioPlugin.executable.linux
        }?.map {
            insertArguments(it)
        }
        return options?.let {
            selectValid(it)
        }
    }

    private fun insertArguments(filename: String): String {
        return filename.replace("\${user.name}", System.getProperty("user.name"))
    }

    private fun selectValid(paths: List<String>): String? {
        return paths.map { File(it) }.firstOrNull {
            it.exists() && it.canExecute()
        }?.absolutePath
    }
}
