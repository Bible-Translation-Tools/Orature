package integrationtest.projects

import org.wycliffeassociates.otter.common.data.model.ContentType

data class RowCount(
    val collections: Int = 0,
    val links: Int = 0,
    val derivatives: Int = 0,
    val contents: Map<ContentType, Int> = mapOf()
)