package org.wycliffeassociates.otter.jvm.workbookapp.plugin.parser

data class ParsedExecutable(
    // nullable since executable might not exist for a platform
    var macos: List<String>?,
    var windows: List<String>?,
    var linux: List<String>?
)
