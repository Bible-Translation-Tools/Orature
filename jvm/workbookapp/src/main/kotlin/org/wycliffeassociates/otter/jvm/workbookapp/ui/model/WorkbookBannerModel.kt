package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import tornadofx.*
import java.io.File

class WorkbookBannerModel(
    title: String,
    val coverArt: File?,
    val onDelete: () -> Unit,
    val onExport: () -> Unit
): WorkbookItemModel(sort = 0, title = title) {
    val rcMetadataProperty = SimpleObjectProperty<ResourceMetadata>()

    var rcTitle: String? = null
    var rcType: ContainerType? = null

    init {
        rcMetadataProperty.onChange {
            it?.let {
                rcTitle = it.title
                rcType = it.type
            }
        }
    }
}
