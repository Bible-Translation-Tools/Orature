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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.TranslationCardSkin
import tornadofx.*

class TranslationCard<T>(
    sourceLanguage: String = "",
    targetLanguage: String = "",
    items: ObservableList<T> = observableListOf()
) : Control() {

    val sourceLanguageProperty = SimpleStringProperty(sourceLanguage)
    val targetLanguageProperty = SimpleStringProperty(targetLanguage)
    val itemsProperty = SimpleListProperty<T>(items)
    val removeTranslationTextProperty = SimpleStringProperty()
    val showMoreTextProperty = SimpleStringProperty()
    val showLessTextProperty = SimpleStringProperty()
    val orientationScaleProperty = SimpleDoubleProperty()

    val onNewBookActionProperty = SimpleObjectProperty<() -> Unit>()
    val shownItemsNumberProperty = SimpleIntegerProperty(3)

    internal val converterProperty = SimpleObjectProperty<(T) -> Node>()
    internal val seeAllProperty = SimpleBooleanProperty(false)

    init {
        styleClass.setAll("translation-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return TranslationCardSkin(this)
    }

    fun setOnNewBookAction(op: () -> Unit) {
        onNewBookActionProperty.set(op)
    }

    fun setConverter(op: (T) -> Node) {
        converterProperty.set(op)
    }
}
