package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.model.MimeType

data class TextItem(
    val text: String,
    val format: MimeType
)
