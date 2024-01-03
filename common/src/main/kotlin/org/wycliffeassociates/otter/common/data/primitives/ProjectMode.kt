package org.wycliffeassociates.otter.common.data.primitives

enum class ProjectMode(val titleKey: String) {
    TRANSLATION("translation"),
    NARRATION("narration"),
    DIALECT("dialect"),
    ;

    companion object {
        fun get(name: String): ProjectMode? {
            return ProjectMode.values().firstOrNull { it.name.lowercase() == name.lowercase() }
        }
    }
}

internal data class SerializableProjectMode(val mode: ProjectMode)
