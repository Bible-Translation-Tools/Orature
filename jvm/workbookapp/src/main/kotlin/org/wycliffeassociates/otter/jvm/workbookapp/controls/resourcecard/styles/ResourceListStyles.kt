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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles

import javafx.scene.paint.Color
import tornadofx.*

typealias LinearU = Dimension<Dimension.LinearUnits>

class ResourceListStyles : Stylesheet() {

    companion object {
        val resourceGroupList by cssclass()
    }

    init {
        resourceGroupList {
            borderColor += box(Color.TRANSPARENT) // Necessary for border under status bar banner to stay visible
            padding = box(0.px, 0.px, 0.px, 80.px) // Left "margin"
            scrollBar {
                +margin(0.px, 0.px, 0.px, 80.px) // Margin between scrollbar and right side of cards
            }

            listCell {
                // Add space between the cards (top margin)
                // But need to make the "margin" at least as large as the dropshadow offsets
                +margin(30.px, 4.px, 0.px, 0.px)
            }
        }
    }

    private fun margin(top: LinearU, right: LinearU, bottom: LinearU, left: LinearU) = mixin {
        padding = box(top, right, bottom, left)
        backgroundInsets += box(top, right, bottom, left)
    }
}