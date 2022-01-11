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

import javafx.beans.property.*
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ResourceTakeCardSkin

class ResourceTakeCard : Control() {

    private val takeProperty = SimpleObjectProperty<Take>()
    private val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    private val takeNumberProperty = SimpleStringProperty("Take 01")
    private val isDraggingProperty = SimpleBooleanProperty(false)

    fun takeProperty(): ObjectProperty<Take> {
        return takeProperty
    }

    fun audioPlayerProperty(): ObjectProperty<IAudioPlayer> {
        return audioPlayerProperty
    }

    fun takeNumberProperty(): StringProperty {
        return takeNumberProperty
    }

    fun isDraggingProperty(): BooleanProperty {
        return isDraggingProperty
    }

    override fun createDefaultSkin(): Skin<*> {
        return ResourceTakeCardSkin(this)
    }
}
