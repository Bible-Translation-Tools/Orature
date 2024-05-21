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
package org.wycliffeassociates.otter.jvm.controls.skins

import javafx.scene.control.SkinBase
import javafx.scene.layout.Priority
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.event.MarkerDeletedEvent
import org.wycliffeassociates.otter.jvm.controls.marker.MarkerNode
import tornadofx.*
import tornadofx.FX.Companion.messages

class MarkerNodeSkin(val control: MarkerNode) : SkinBase<MarkerNode>(control) {

    init {
        val root = buildMarkerSkin()
        children.add(root)
    }

    private fun buildMarkerSkin(): VBox {
        return VBox().apply {
            vgrow = Priority.ALWAYS
            isPickOnBounds = false

            hbox {
                addClass("title-section")
                hgrow = Priority.ALWAYS

                label(control.markerNumberProperty) {
                    addClass("normal-text")
                    graphic = FontIcon(Material.BOOKMARK_OUTLINE).addClass("wa-icon")
                    minWidth = USE_PREF_SIZE
                }
                button {
                    addClass("btn", "btn--tertiary", "normal-text")
                    graphic = FontIcon(MaterialDesign.MDI_DELETE).addClass("wa-icon")
                    tooltip(messages["remove_chunk"])
                    visibleWhen { control.canBeDeletedProperty }
                    managedWhen(visibleProperty())

                    action {
                        FX.eventbus.fire(MarkerDeletedEvent(control.markerIdProperty.value))
                    }
                }
            }
            region {
                isPickOnBounds = false
                vgrow = Priority.ALWAYS
            }
            button {
                addClass("btn", "btn--icon", "marker-node__drag-button")
                graphic = FontIcon(Material.DRAG_HANDLE).apply { rotate = 90.0 }
                tooltip(messages["move_chunk"])
                translateXProperty().bind(widthProperty().divide(2).negate())

                visibleWhen { control.canBeMovedProperty }
                managedWhen(visibleProperty())

                // delegates the mouse drag events to the "drag" button
                setOnMousePressed {
                    control.requestFocus()
                    control.onDragStartProperty.value?.handle(it)
                }
                onMouseDraggedProperty().bind(control.onDragProperty)
                onMouseReleasedProperty().bind(control.onDragFinishProperty)
            }
        }
    }
}
