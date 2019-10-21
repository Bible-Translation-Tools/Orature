package org.wycliffeassociates.otter.common.data.config

data class Initialization(
    var name: String,
    var version: String,
    var initialized: Boolean = false,
    var id: Int = 0
)