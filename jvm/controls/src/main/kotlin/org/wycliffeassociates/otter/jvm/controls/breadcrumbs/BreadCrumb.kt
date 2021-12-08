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
package org.wycliffeassociates.otter.jvm.controls.breadcrumbs

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class BreadCrumb : HBox() {

    val iconProperty = SimpleObjectProperty<FontIcon>()
    val titleProperty = SimpleStringProperty()
    val isActiveProperty = SimpleBooleanProperty(false)
    val tooltipTextProperty = SimpleStringProperty()
    val orientationScaleProperty = SimpleDoubleProperty()
    val onClickProperty = SimpleObjectProperty<EventHandler<MouseEvent>>()

    init {
        styleClass.setAll("breadcrumb")

        label {
            graphicProperty().bind(iconProperty)
            textProperty().bind(titleProperty)
            tooltip { textProperty().bind(titleProperty) }

            addClass("breadcrumb__content")

            isActiveProperty.onChange {
                if (it) {
                    addClass("breadcrumb--active")
                } else {
                    removeClass("breadcrumb--active")
                }
            }

            onMouseClickedProperty().bind(onClickProperty)
        }

        label {
            addClass("breadcrumb__separator")
            graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT).apply {
                scaleXProperty().bind(orientationScaleProperty)
            }
            hiddenWhen(isActiveProperty)
            managedWhen(visibleProperty())
        }

        label {
            addClass("breadcrumb__help")

            graphic = FontIcon(MaterialDesign.MDI_HELP_CIRCLE)
            visibleWhen(
                Bindings.and(isActiveProperty, tooltipTextProperty.isNotEmpty)
            )
            managedWhen(visibleProperty())

            tooltip {
                textProperty().bind(tooltipTextProperty)
                prefWidth = 256.0
            }
        }
    }

    fun onClickAction(op: () -> Unit) {
        onClickProperty.set(EventHandler { op() })
    }
}
