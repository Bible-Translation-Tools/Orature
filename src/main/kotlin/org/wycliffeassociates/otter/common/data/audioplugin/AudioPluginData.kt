package org.wycliffeassociates.otter.common.data.audioplugin

import java.io.File

data class AudioPluginData(
        var id: Int,
        var name: String,
        var version: String,
        var canEdit: Boolean,
        var canRecord: Boolean,
        var executable: String,
        var args: List<String>,
        var pluginFile: File?
)