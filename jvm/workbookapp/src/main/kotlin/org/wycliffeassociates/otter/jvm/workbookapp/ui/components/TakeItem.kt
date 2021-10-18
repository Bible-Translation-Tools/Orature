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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.ListAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*
import tornadofx.FX.Companion.messages

class TakeItem : HBox() {
    val takeProperty = SimpleObjectProperty<TakeModel>()
    val selectedProperty = SimpleBooleanProperty(false)
    val animationMediatorProperty =
        SimpleObjectProperty<ListAnimationMediator<TakeItem>>()

    private val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val selectedIcon = FontIcon(MaterialDesign.MDI_CHECK)
    private val promoteIcon = FontIcon(MaterialDesign.MDI_ARROW_UP)

    init {
        styleClass.setAll("take-item")

        simpleaudioplayer {
            hgrow = Priority.ALWAYS

            takeProperty.onChange { take ->
                take?.let {
                    playerProperty.set(take.audioPlayer)
                }
            }
        }

        button {
            addClass("btn", "btn--icon")
            tooltip(messages["select"])
            graphicProperty().bind(selectedProperty.objectBinding {
                when (it) {
                    true -> selectedIcon
                    else -> promoteIcon
                }
            })
            selectedProperty.onChange {
                togglePseudoClass("selected", it)
            }
            setOnAction {
                animationMediatorProperty.value?.let {
                    if (it.isAnimating || selectedProperty.value) {
                        return@setOnAction
                    }
                    it.node = this@TakeItem
                    it.animate {
                        onTakeSelectedActionProperty.value?.handle(ActionEvent())
                    }
                } ?: onTakeSelectedActionProperty.value?.handle(ActionEvent())
            }
        }
    }

    fun setOnTakeSelected(op: () -> Unit) {
        onTakeSelectedActionProperty.set(EventHandler { op.invoke() })
    }
}
