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
package org.wycliffeassociates.otter.jvm.controls.navigation

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.styles.AppTheme
import tornadofx.*

class ProjectNavStyles : Stylesheet() {

    companion object {
        val navbutton by cssclass()
        val navBoxInnercard by cssclass()
        val cardLabel by cssclass()
    }

    init {

        navBoxInnercard {
            backgroundColor += AppTheme.colors.lightBackground
            borderColor += box(Color.WHITE)
            borderWidth += box(3.0.px)
            borderRadius += box(5.0.px)
            borderInsets += box(1.5.px)
        }

        navbutton {
            backgroundColor += AppTheme.colors.white
            textFill = AppTheme.colors.defaultText
            borderColor += box(AppTheme.colors.lightBackground)
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            effect = DropShadow(2.0, 2.0, 2.0, AppTheme.colors.defaultBackground)
            prefWidth = 90.px
        }
        cardLabel {
            effect = DropShadow(25.0, 2.0, 2.0, c("#FBFEFF"))
            fontSize = 24.px
        }
    }
}
