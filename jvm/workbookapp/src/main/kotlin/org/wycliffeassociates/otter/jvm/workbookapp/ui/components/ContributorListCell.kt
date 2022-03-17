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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import tornadofx.*

class ContributorListCell : ListCell<Contributor>() {
    private val cellGraphic = ContributorCell()

    val onRemoveContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onEditContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    override fun updateItem(item: Contributor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = cellGraphic.apply {
            nameProperty.set(item.name)
            indexProperty.set(index)
            onRemoveContributorActionProperty.bind(this@ContributorListCell.onRemoveContributorActionProperty)
            onEditContributorActionProperty.bind(this@ContributorListCell.onEditContributorActionProperty)
        }
    }
}

class ContributorCell : HBox() {
    val indexProperty = SimpleIntegerProperty(-1)
    val nameProperty = SimpleStringProperty()
    val onRemoveContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(null)
    val onEditContributorActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(null)

    init {
        hgrow = Priority.ALWAYS
        addClass("contributor__list-cell")

        textfield(nameProperty) {
            hgrow = Priority.ALWAYS
            addClass("txt-input", "contributor__text-input")

            focusedProperty().onChange { focused ->
                if (!focused) {
                    // save changes when user click other control
                    onEditContributorActionProperty.value?.handle(
                        ActionEvent(
                            ContributorCellData(indexProperty.value, nameProperty.value),
                            null
                        )
                    )
                }
            }
        }
        button {
            addClass("btn", "btn--icon", "contributor__list-cell__delete-btn")
            graphic = FontIcon(Material.DELETE)
            setOnAction {
                onRemoveContributorActionProperty.value?.handle(
                    ActionEvent(indexProperty.value, this@ContributorCell)
                )
            }
        }
    }
}