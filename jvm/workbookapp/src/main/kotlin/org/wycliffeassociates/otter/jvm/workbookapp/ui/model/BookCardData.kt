package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.primitives.Collection
import java.io.File

data class BookCardData(val collection: Collection, val artwork: File? = null)

