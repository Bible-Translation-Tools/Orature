/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ChapterSelector : StackPane() {
    val chapterTitleProperty = SimpleStringProperty()
    val onChapterSelectorOpenedProperty =
        SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPrevChapterActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onNextChapterActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val prevDisabledProperty = SimpleBooleanProperty()
    val nextDisabledProperty = SimpleBooleanProperty()

    init {
        addClass("chapter-selector")
        hbox {
            addClass("chapter-selector__controls")
            button {
                addClass("btn", "btn--tertiary", "chapter-selector__btn-prev")
                graphic = FontIcon(MaterialDesign.MDI_CHEVRON_LEFT)
                disableProperty().bind(prevDisabledProperty)
                onActionProperty().bind(onPrevChapterActionProperty)
            }
            button(chapterTitleProperty) {
                addClass("chapter-selector__title")
                graphic = FontIcon(MaterialDesign.MDI_FILE)
                fitToParentHeight()
                disableProperty().bind(prevDisabledProperty.and(nextDisabledProperty))
                onActionProperty().bind(onChapterSelectorOpenedProperty)
            }
            button {
                addClass("btn", "btn--tertiary", "chapter-selector__btn-next")
                graphic = FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT)
                disableProperty().bind(nextDisabledProperty)
                onActionProperty().bind(onNextChapterActionProperty)
            }
        }
    }

    fun setOnPreviousChapter(op: () -> Unit) {
        onPrevChapterActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnNextChapter(op: () -> Unit) {
        onNextChapterActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnChapterSelectorOpenedProperty(op: () -> Unit) {
        onChapterSelectorOpenedProperty.set(EventHandler { op.invoke() })
    }
}

fun EventTarget.chapterSelector(op: ChapterSelector.() -> Unit) = ChapterSelector().attachTo(this, op)