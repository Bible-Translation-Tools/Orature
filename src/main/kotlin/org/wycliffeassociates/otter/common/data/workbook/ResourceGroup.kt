package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable

data class ResourceGroup(
    val info: ResourceInfo,
    val resources: Observable<Resource>
)
