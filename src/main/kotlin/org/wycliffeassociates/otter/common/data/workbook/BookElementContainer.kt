package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable

interface BookElementContainer {
    val children: Observable<BookElement>
    val subtreeResources: List<ResourceInfo>
    // val progress: Observable<Int>
}
