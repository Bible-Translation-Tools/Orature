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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.styles

import javafx.scene.paint.Color
import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.*

class MainScreenStyles : Stylesheet() {
    companion object {
        val main by cssclass()
        val listMenu by cssclass()
        val listItem by cssclass()
        val navBoxInnercard by cssclass()
        val navbutton by cssclass()
        val singleTab by cssclass()
    }
    private val headerAreaBackgroundColor = AppTheme.colors.appDarkGrey

    init {
        main {
            prefWidth = Screen.getPrimary().visualBounds.width.px - 100.0
            prefHeight = Screen.getPrimary().visualBounds.height.px - 100.0
        }

        // this gets compiled down to list-item
        listItem {
            backgroundColor += headerAreaBackgroundColor
            padding = box(24.px)
        }

        tabHeaderArea {
            backgroundColor += Color.TRANSPARENT
        }

        tabPane {
            backgroundColor += headerAreaBackgroundColor
        }

        s(tabPane and singleTab) {
            tabHeaderArea {
                prefHeight = 0.px
            }
        }
    }
}
