package org.wycliffeassociates.otter.common.data.primitives

enum class CheckingStatus {
    UNCHECKED,
    PEER_EDIT,
    KEYWORD,
    VERSE;

    companion object {
        fun get(name: String): CheckingStatus? {
            return CheckingStatus.values().firstOrNull { it.name.lowercase() == name.lowercase() }
        }
    }
}