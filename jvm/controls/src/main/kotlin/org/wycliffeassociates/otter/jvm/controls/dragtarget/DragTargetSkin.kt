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
package org.wycliffeassociates.otter.jvm.controls.dragtarget

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import tornadofx.*

abstract class DragTargetSkin(
    control: DragTarget,
    dragTargetSize: CssRule,
    placeholder: CssRule,
    borderGlow: CssRule,
    dragTargetOverlay: CssRule
) : SkinBase<DragTarget>(control) {
    protected var selectedTakePlaceholder: Node by singleAssign()

    init {
        val root = StackPane().apply {
            addClass(dragTargetSize)
            selectedTakePlaceholder = vbox {
                addClass(placeholder)
                toggleClass(borderGlow, skinnable.dragBinding)
            }
            vbox {
                bindSingleChild(skinnable.selectedNodeProperty)
            }
            vbox {
                addClass(dragTargetOverlay)
                alignment = Pos.CENTER
                add(MaterialIconView(MaterialIcon.ADD, "30px"))
                visibleProperty().bind(skinnable.dragBinding)
            }
        }
        children.addAll(root)
    }
}