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

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import java.util.concurrent.Callable

class BreadcrumbBar : HBox() {

    private val items = observableListOf<BreadCrumb>()

    val orientationScaleProperty = SimpleDoubleProperty()

    init {
        tryImportStylesheet(javaClass.getResource("/css/breadcrumb-bar.css").toExternalForm())
        styleClass.setAll("breadcrumb-bar")

        bindChildren(items) { breadcrumb ->
            breadcrumb.orientationScaleProperty.bind(orientationScaleProperty)
            breadcrumb.isActiveProperty.bind(
                Bindings.createBooleanBinding(
                    Callable { items.last() == breadcrumb },
                    items
                )
            )
            breadcrumb
        }
    }

    fun addItem(item: BreadCrumb) {
        if (items.contains(item).not()) {
            items.add(item)
        }
        removeItemAfter(item)
    }

    fun removeItem(item: BreadCrumb) {
        items.remove(item)
    }

    private fun removeItemAfter(item: BreadCrumb) {
        val fromIndex = items.indexOf(item) + 1
        items.remove(fromIndex, items.size)
    }
}
