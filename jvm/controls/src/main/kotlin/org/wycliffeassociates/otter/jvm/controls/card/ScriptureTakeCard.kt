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
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.ListAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ScriptureTakeCardSkin
import tornadofx.stringBinding
import java.text.SimpleDateFormat
import java.util.*

class ScriptureTakeCard : Control() {

    val takeProperty = SimpleObjectProperty<Take>()
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val selectedProperty = SimpleBooleanProperty()
    val takeLabelProperty = SimpleStringProperty()
    val lastModifiedProperty = SimpleStringProperty()
    val deletedProperty = SimpleBooleanProperty()
    val animationMediatorProperty =
        SimpleObjectProperty<ListAnimationMediator<ScriptureTakeCard>>()

    val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onTakeDeleteActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onTakeEditActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        lastModifiedProperty.bind(takeProperty.stringBinding {
            it?.let {
                SimpleDateFormat.getDateTimeInstance(
                    SimpleDateFormat.SHORT,
                    SimpleDateFormat.SHORT,
                    Locale.getDefault()
                ).format(it.file.lastModified())
            }
        })
    }

    fun setOnTakeDelete(op: () -> Unit) {
        onTakeDeleteActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnTakeEdit(op: () -> Unit) {
        onTakeEditActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnTakeSelected(op: () -> Unit) {
        onTakeSelectedActionProperty.set(EventHandler { op.invoke() })
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScriptureTakeCardSkin(this)
    }
}
