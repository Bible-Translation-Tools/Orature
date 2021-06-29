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
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.*
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ScriptureTakeCardSkin

class ScriptureTakeCard : Control() {

    private val takeProperty = SimpleObjectProperty<Take>()
    private val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    private val deleteTextProperty = SimpleStringProperty("delete")
    private val editTextProperty = SimpleStringProperty("edit")
    private val markerTextProperty = SimpleStringProperty("marker")
    private val playTextProperty = SimpleStringProperty("play")
    private val pauseTextProperty = SimpleStringProperty("pause")
    private val takeNumberProperty = SimpleStringProperty("Take 01")
    private val timestampProperty = SimpleStringProperty("")
    private val isDraggingProperty = SimpleBooleanProperty(false)
    private val allowMarkerProperty = SimpleBooleanProperty(true)

    fun takeProperty(): ObjectProperty<Take> {
        return takeProperty
    }

    fun audioPlayerProperty(): ObjectProperty<IAudioPlayer> {
        return audioPlayerProperty
    }

    fun deleteTextProperty(): StringProperty {
        return deleteTextProperty
    }

    fun editTextProperty(): StringProperty {
        return editTextProperty
    }

    fun markerTextProperty(): StringProperty {
        return markerTextProperty
    }

    fun playTextProperty(): StringProperty {
        return playTextProperty
    }

    fun pauseTextProperty(): StringProperty {
        return pauseTextProperty
    }

    fun takeNumberProperty(): StringProperty {
        return takeNumberProperty
    }

    fun timestampProperty(): StringProperty {
        return timestampProperty
    }

    fun isDraggingProperty(): BooleanProperty {
        return isDraggingProperty
    }

    fun allowMarkerProperty(): BooleanProperty {
        return allowMarkerProperty
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScriptureTakeCardSkin(this)
    }
}
