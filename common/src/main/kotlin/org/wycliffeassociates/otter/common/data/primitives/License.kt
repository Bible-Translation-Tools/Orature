package org.wycliffeassociates.otter.common.data.primitives

enum class License(val title: String, val url: String?) {
    CCBYSA4_0("CC BY-SA 4.0", "https://creativecommons.org/licenses/by-sa/4.0/");

    companion object {
        fun get(title: String): License? {
            return values().firstOrNull { it.title == title }
        }
    }
}