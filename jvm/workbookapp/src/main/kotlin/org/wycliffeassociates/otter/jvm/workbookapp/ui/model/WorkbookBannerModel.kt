/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import tornadofx.*
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork

class WorkbookBannerModel(
    title: String,
    val coverArt: Artwork?,
    val onDelete: () -> Unit,
    val onExport: () -> Unit
) : WorkbookItemModel(sort = 0, title = title) {
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
