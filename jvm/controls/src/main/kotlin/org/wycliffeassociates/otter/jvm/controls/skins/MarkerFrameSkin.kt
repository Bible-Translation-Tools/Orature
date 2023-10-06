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
package org.wycliffeassociates.otter.jvm.controls.skins

import javafx.scene.control.SkinBase
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.MarkerFrame
import tornadofx.*

class MarkerFrameSkin(val control: MarkerFrame) : SkinBase<MarkerFrame>(control) {

    init {
        val root = buildMarkerSkin()
        children.add(root)
    }

    private fun buildMarkerSkin(): VBox {
        return VBox().apply {
            vgrow = Priority.ALWAYS

            hbox {
                addClass("title-section")
                hgrow = Priority.ALWAYS

                label(control.markerNumberProperty) {
                    addClass("normal-text")
                    graphic = FontIcon(Material.BOOKMARK_OUTLINE).addClass("wa-icon")
                }
                label {
                    addClass("normal-text")
                    graphic = FontIcon(MaterialDesign.MDI_DELETE).addClass("wa-icon")
                }
            }
            region { vgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon")
                graphic = FontIcon(Material.DRAG_HANDLE)
                rotate = 90.0
            }
        }
    }
}
