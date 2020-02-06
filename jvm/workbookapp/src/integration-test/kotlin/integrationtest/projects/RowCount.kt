package integrationtest.projects

import org.wycliffeassociates.otter.common.data.model.ContentType

data class RowCount(
    val collections: Int? = null,
    val links: Int? = null,
    val derivatives: Int? = null,
    val contents: Map<ContentType, Int>? = null
)