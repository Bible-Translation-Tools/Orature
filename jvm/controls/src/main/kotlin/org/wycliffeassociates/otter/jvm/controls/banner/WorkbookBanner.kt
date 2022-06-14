/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.banner

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import org.wycliffeassociates.otter.jvm.controls.skins.banner.WorkbookBannerSkin
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WorkbookBanner : Control() {

    val backgroundArtworkProperty = SimpleObjectProperty<Artwork>()
    val attributionTextProperty = SimpleStringProperty()
    val bookTitleProperty = SimpleStringProperty()
    val resourceTitleProperty = SimpleStringProperty()
    val hideDeleteButtonProperty = SimpleBooleanProperty(false)

    val deleteTitleProperty = SimpleStringProperty("delete")
    val exportTitleProperty = SimpleStringProperty("export")

    val onDeleteActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onExportActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val exportOptions = observableListOf(
        ExportOption.LISTEN,
        ExportOption.SOURCE_AUDIO,
        ExportOption.BACKUP
    )

    init {
        styleClass.setAll("workbook-banner")
        backgroundArtworkProperty.onChangeAndDoNow { artwork ->
            artwork?.let {
                attributionTextProperty.set(
                    it.attributionText(
                        FX.messages["artworkLicense"],
                        FX.messages["artworkAttributionTitle"],
                        FX.messages["license"]
                    )
                )
            } ?: attributionTextProperty.set(null)
        }
    }

    fun coverImageBinding(): ObjectBinding<Image> {
        return Bindings.createObjectBinding(
            {
                backgroundArtworkProperty.value?.let {
                    it.file.inputStream().use { input ->
                        Image(input)
                    }
                }
            },
            backgroundArtworkProperty
        )
    }

    fun onDeleteAction(op: () -> Unit) {
        onDeleteActionProperty.set(EventHandler { op.invoke() })
    }

    fun onExportAction(op: (ExportOption) -> Unit) {
        onExportActionProperty.set(
            EventHandler { op.invoke(it.source as ExportOption) }
        )
    }

    fun cleanUp() {
        backgroundArtworkProperty.unbind()
        backgroundArtworkProperty.set(null)
    }

    override fun createDefaultSkin(): Skin<*> {
        return WorkbookBannerSkin(this)
    }
}
