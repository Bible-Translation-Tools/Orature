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
package org.wycliffeassociates.otter.jvm.controls.styles

import javafx.scene.paint.Color
import tornadofx.c

abstract class AppColors {
    val white: Color = Color.WHITE
    val appRed: Color = c("#CC4141")
    val appBlue: Color = c("#0094F0")
    val appGreen: Color = c("#58BD2F")
    val appDarkGrey: Color = c("#E0E0E0")

    abstract val base: Color
    abstract val defaultBackground: Color
    abstract val defaultText: Color
    abstract val subtitle: Color
    abstract val cardBackground: Color
    abstract val disabledCardBackground: Color
    abstract val colorlessButton: Color
    abstract val dropShadow: Color
    abstract val imagePlaceholder: Color
    abstract val lightBackground: Color
}