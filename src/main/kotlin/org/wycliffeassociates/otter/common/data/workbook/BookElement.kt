package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable

interface BookElement {
    val sort: Int
    val title: String
    val audio: AssociatedAudio
    val hasResources: Boolean
    val resources: Observable<Resource>
}
