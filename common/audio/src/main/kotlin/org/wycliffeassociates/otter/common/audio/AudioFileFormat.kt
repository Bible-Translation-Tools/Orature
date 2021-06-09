package org.wycliffeassociates.otter.common.audio

enum class AudioFileFormat(val extension: String) {
    WAV("wav"),
    MP3("mp3");

    companion object {
        private val map = values().associateBy { it.extension.toLowerCase() }

        /** @throws IllegalArgumentException */
        fun of(extension: String) =
            map[extension.toLowerCase()]
                ?: throw IllegalArgumentException("Audio extension $extension not supported")
    }
}
