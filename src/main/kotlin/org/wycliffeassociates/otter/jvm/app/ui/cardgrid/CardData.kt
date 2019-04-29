package org.wycliffeassociates.otter.jvm.app.ui.cardgrid

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content

data class CardData(
    val item: String,
    val dataType: String,
    val bodyText : String,
    val sort: Int,
    val contentSource: Content? =null,
    val collectionSource: Collection? =null

)