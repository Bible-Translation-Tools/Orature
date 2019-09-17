package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

interface BookElementContainer {
    val children: Observable<BookElement>
    val subtreeResources: List<ResourceMetadata>
    // val progress: Observable<Int>
}
