package org.wycliffeassociates.otter.common.data.primitives

enum class CheckingLevel {
    UNCHECKED,
    PEER_EDIT,
    KEYWORD,
    VERSE;

    companion object {
        fun get(name: String): CheckingLevel? {
            return CheckingLevel.values().firstOrNull { it.name.lowercase() == name.lowercase() }
        }
    }
}