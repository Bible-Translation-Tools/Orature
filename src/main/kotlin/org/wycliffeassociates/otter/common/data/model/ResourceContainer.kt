package org.wycliffeassociates.otter.common.data.model

import java.io.File
import java.util.Calendar

data class ResourceContainer(
        var conformsTo: String,
        var creator: String,
        var description: String,
        var format: String,
        var identifier: String,
        var issued: Calendar,
        var language: Language,
        var modified: Calendar,
        var publisher: String,
        var subject: String,
        var type: String,
        var title: String,
        var version: Int,
        var path: File,
        var id: Int = 0
)