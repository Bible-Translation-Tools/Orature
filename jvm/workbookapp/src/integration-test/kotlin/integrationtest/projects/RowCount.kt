package integrationtest.projects

import org.wycliffeassociates.otter.common.data.model.ContentType

data class RowCount(
    val collections: Int,
    val links: Int,
    val contents: Map<ContentType, Int>
)