package org.wycliffeassociates.otter.jvm.device.audioplugin.parser

data class ParsedExecutable(
        // nullable since executable might not exist for a platform
        var macos: String?,
        var windows: String?,
        var linux: String?
)