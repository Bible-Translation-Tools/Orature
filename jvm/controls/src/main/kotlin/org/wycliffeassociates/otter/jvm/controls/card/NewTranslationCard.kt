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
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.NewTranslationCardSkin

class NewTranslationCard : Control() {

    val sourceLanguageProperty = SimpleStringProperty("???")
    val targetLanguageProperty = SimpleStringProperty("???")
    val orientationScaleProperty = SimpleDoubleProperty()

    val newTranslationTextProperty = SimpleStringProperty()
    val onActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("new-translation-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return NewTranslationCardSkin(this)
    }

    fun setOnAction(op: () -> Unit) {
        onActionProperty.set(EventHandler { op.invoke() })
    }
}
