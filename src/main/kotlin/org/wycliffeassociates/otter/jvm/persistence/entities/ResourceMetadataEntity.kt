package org.wycliffeassociates.otter.jvm.persistence.entities

data class ResourceMetadataEntity(
        var id: Int,
        var conformsTo: String,
        var creator: String,
        var description: String,
        var format: String,
        var identifier: String,
        var issued: String,
        var languageFk: Int,
        var modified: String,
        var publisher: String,
        var subject: String,
        var type: String,
        var title: String,
        var version: String,
        var path: String,
        var derivedFromFk: Int?
)