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
package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.chapterselector.ChapterSelectorSkin
import tornadofx.*

class ChapterSelector : Control() {
    val chapterTitleProperty = SimpleStringProperty()
    val onPrevChapterActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onNextChapterActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val prevDisabledProperty = SimpleBooleanProperty()
    val nextDisabledProperty = SimpleBooleanProperty()

    init {
        styleClass.setAll("chapter-selector")
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChapterSelectorSkin(this)
    }

    fun setOnPreviousChapter(op: () -> Unit) {
        onPrevChapterActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnNextChapter(op: () -> Unit) {
        onNextChapterActionProperty.set(EventHandler { op.invoke() })
    }
}

fun EventTarget.chapterSelector(op: ChapterSelector.() -> Unit) = ChapterSelector().attachTo(this, op)