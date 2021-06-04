package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Language

data class Translation(
    var source: Language,
    var target: Language,
    var id: Int = 0
)
