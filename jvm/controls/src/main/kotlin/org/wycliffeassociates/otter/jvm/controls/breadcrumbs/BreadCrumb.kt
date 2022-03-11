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
package org.wycliffeassociates.otter.jvm.controls.breadcrumbs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.scene.control.ButtonBase
import javafx.scene.control.Skin
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.skins.breadcrumb.BreadCrumbSkin
import tornadofx.*

class BreadCrumb : ButtonBase() {

    val iconProperty = SimpleObjectProperty<FontIcon>()
    val titleProperty = SimpleStringProperty()
    val isActiveProperty = SimpleBooleanProperty(false)
    val tooltipTextProperty = SimpleStringProperty()
    val orientationScaleProperty = SimpleDoubleProperty()

    init {
        styleClass.setAll("breadcrumb")

        isActiveProperty.onChange {
            if (it) {
                addClass("breadcrumb--active")
            } else {
                removeClass("breadcrumb--active")
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return BreadCrumbSkin(this)
    }

    override fun fire() {
        if (!isDisabled) {
            fireEvent(ActionEvent())
        }
    }
}
