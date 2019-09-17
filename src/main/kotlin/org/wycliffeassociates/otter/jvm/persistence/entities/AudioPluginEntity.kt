package org.wycliffeassociates.otter.jvm.persistence.entities

data class AudioPluginEntity(
    var id: Int,
    var name: String,
    var version: String,
    var bin: String,
    var args: String,
    var edit: Int,
    var record: Int,
    var path: String?
)