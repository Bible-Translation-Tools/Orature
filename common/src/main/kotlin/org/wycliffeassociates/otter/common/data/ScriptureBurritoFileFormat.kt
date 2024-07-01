package org.wycliffeassociates.otter.common.data

enum class ScriptureBurritoFileFormat(val extension: String) {
    BURRITO("burrito"),
    ZIP("zip");

    companion object {
        val extensionList: List<String> = entries.map { it.extension }

        fun isSupported(extension: String) = extension.lowercase() in extensionList
    }
}