package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable

data class Chunk(
    override val sort: Int,
    override val title: String,
    override val audio: AssociatedAudio,
    override val hasResources: Boolean,
    override val resources: Observable<Resource>,

    val text: TextItem?
) : BookElement
