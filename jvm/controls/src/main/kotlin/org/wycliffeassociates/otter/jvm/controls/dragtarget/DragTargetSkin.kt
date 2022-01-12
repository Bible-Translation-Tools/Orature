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
package org.wycliffeassociates.otter.jvm.controls.dragtarget

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import tornadofx.*

abstract class DragTargetSkin(
    control: DragTarget
) : SkinBase<DragTarget>(control) {
    protected var selectedTakePlaceholder: Node by singleAssign()

    init {
        val root = StackPane().apply {
            selectedTakePlaceholder = vbox {
                addClass("card--take__placeholder")
                skinnable.dragBinding.onChange {
                    toggleClass("card--take__border-glow", it)
                }
            }
            vbox {
                bindSingleChild(skinnable.selectedNodeProperty)
            }
            vbox {
                addClass("card--take__dragtarget-overlay")
                alignment = Pos.CENTER
                label {
                    addClass("card--take__add")
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)
                }
                visibleProperty().bind(skinnable.dragBinding)
            }
        }
        children.addAll(root)
    }
}
