package org.wycliffeassociates.otter.common.data

enum class TstudioFileFormat(val extension: String) {
    TSTUDIO("tstudio"),
    ZIP("zip");

    companion object {
        val extensionList: List<String> = OratureFileFormat.values().map { it.extension }

        fun isSupported(extension: String) = extension.lowercase() in extensionList
    }
}