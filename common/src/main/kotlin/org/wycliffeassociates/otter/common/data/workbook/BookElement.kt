package org.wycliffeassociates.otter.common.data.workbook

interface BookElement {
    val sort: Int
    val title: String
    val label: String
    val audio: AssociatedAudio
    val resources: List<ResourceGroup>
}
