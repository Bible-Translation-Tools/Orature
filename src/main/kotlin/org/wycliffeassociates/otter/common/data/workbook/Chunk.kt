package org.wycliffeassociates.otter.common.data.workbook

data class Chunk(
    override val sort: Int,
    override val title: String,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,

    val text: TextItem?

) : BookElement
