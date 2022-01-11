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
package org.wycliffeassociates.otter.jvm.controls.banner

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork
import org.wycliffeassociates.otter.jvm.controls.skins.banner.ResumeBookBannerSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ResumeBookBanner : Control() {

    val backgroundArtworkProperty = SimpleObjectProperty<Artwork>()
    val attributionTextProperty = SimpleStringProperty()
    val bookTitleProperty = SimpleStringProperty()
    val sourceLanguageProperty = SimpleStringProperty()
    val targetLanguageProperty = SimpleStringProperty()
    val resumeTextProperty = SimpleStringProperty("Resume")
    val orientationScaleProperty = SimpleDoubleProperty()
    val onResumeActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("resume-book-banner")
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

    fun onResumeAction(op: () -> Unit) {
        onResumeActionProperty.set(EventHandler { op.invoke() })
    }

    fun coverImageBinding(): ObjectBinding<Image> {
        return Bindings.createObjectBinding(
            {
                backgroundArtworkProperty.value?.let {
                    Image(it.file.inputStream())
                }
            },
            backgroundArtworkProperty
        )
    }

    override fun createDefaultSkin(): Skin<*> {
        return ResumeBookBannerSkin(this)
    }
}
