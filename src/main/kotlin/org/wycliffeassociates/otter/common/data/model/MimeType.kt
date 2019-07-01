package org.wycliffeassociates.otter.common.data.model

enum class MimeType(vararg types: String) {
    USFM("text/usfm", "text/x-usfm", "usfm"),
    MARKDOWN("text/markdown", "text/x-markdown", "markdown"),
    WAV("audio/wav", "audio/wave", "audio/x-wave", "audio/vnd.wave");

    val accepted = types.toList()
    val norm = accepted.first()

    companion object {
        private val map: Map<String, MimeType> = values()
            .flatMap { mt -> mt.accepted.map { it to mt } }
            .associate { it }

        fun of(type: String) = map[type.toLowerCase()]
    }
}
