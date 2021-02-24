package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

data class ResourceGroup(
    val metadata: ResourceMetadata,
    val resources: Observable<Resource>
)
