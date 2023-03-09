package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

open class ProjectMetadata(
    val resourceSlug: String,
    val language: Language,
    val creator: String = "Orature"
) {
    constructor(resourceMetadata: ResourceMetadata): this(
        resourceSlug = resourceMetadata.identifier,
        language = resourceMetadata.language,
        creator = resourceMetadata.creator
    )
}
