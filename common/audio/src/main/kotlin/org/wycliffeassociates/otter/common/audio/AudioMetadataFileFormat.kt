package org.wycliffeassociates.otter.common.audio

enum class AudioMetadataFileFormat(val extension: String) {
    CUE("cue");

    companion object {
        private val extensionList: List<String> = values().map { it.extension }
        private val map = values().associateBy { it.extension.lowercase() }

        /** @throws IllegalArgumentException */
        fun of(extension: String) =
            map[extension.lowercase()]
                ?: throw IllegalArgumentException("Audio extension $extension not supported")

        fun isSupported(extension: String) = extension.lowercase() in extensionList
    }
}
