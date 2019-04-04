package org.wycliffeassociates.otter.common.data.workbook

data class Resource(
    val sort: Int,
    val title: TextItem,
    val body: TextItem?,
    val titleAudio: AssociatedAudio,
    val bodyAudio: AssociatedAudio?
)
