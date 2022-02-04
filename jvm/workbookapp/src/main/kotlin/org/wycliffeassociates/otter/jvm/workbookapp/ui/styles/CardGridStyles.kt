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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.styles

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.*

class CardGridStyles : Stylesheet() {

    companion object {
        val contentContainer by cssclass()
        val contentLoadingProgress by cssclass()
    }

    init {
        contentContainer {
            vgap = 32.px
            hgap = 24.px
            alignment = Pos.CENTER
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            // Add larger padding on bottom to keep FAB from blocking last row cards
        }

        contentLoadingProgress {
            progressColor = AppTheme.colors.appRed
        }
    }
}
