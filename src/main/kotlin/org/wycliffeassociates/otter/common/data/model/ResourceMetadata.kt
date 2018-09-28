package org.wycliffeassociates.otter.common.data.model

import java.io.File
import java.time.ZonedDateTime

data class ResourceMetadata(
        var conformsTo: String,
        var creator: String,
        var description: String,
        var format: String,
        var identifier: String,
        var issued: ZonedDateTime,
        var language: Language,
        var modified: ZonedDateTime,
        var publisher: String,
        var subject: String,
        var type: String,
        var title: String,
        var version: String,
        var path: File,
        var id: Int = 0
)