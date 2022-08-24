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

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import tornadofx.*

class LanguageCardCellSkin(private val cell: LanguageCardCell) : SkinBase<LanguageCardCell>(cell) {
    private val behavior = ButtonBehavior(cell)

    init {
        children.addAll(
            HBox().apply {
                addClass("language-card-cell__root")

                label {
                    addClass("language-card-cell__icon")
                    graphicProperty().bind(cell.iconProperty)
                }

                vbox {
                    addClass("language-card-cell__title")
                    label(cell.languageNameProperty).apply {
                        addClass("language-card-cell__name")
                    }
                    label(cell.languageSlugProperty).apply {
                        addClass("language-card-cell__slug")
                    }
                }
            }
        )
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
