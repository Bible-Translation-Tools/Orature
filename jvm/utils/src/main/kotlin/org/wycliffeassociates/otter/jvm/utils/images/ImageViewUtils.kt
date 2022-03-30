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
package org.wycliffeassociates.otter.jvm.utils.images

import javafx.scene.image.ImageView
import javafx.scene.layout.Region

fun ImageView.fitToParentHeight() {
    val parent = this.parent
    if (parent != null && parent is Region) {
        fitToHeight(parent)
    }
}

fun ImageView.fitToParentWidth() {
    val parent = this.parent
    if (parent != null && parent is Region) {
        fitToWidth(parent)
    }
}

fun ImageView.fitToParentSize() {
    fitToParentHeight()
    fitToParentWidth()
}

fun ImageView.fitToHeight(region: Region) {
    fitHeightProperty().bind(region.heightProperty())
}

fun ImageView.fitToWidth(region: Region) {
    fitWidthProperty().bind(region.widthProperty())
}

fun ImageView.fitToSize(region: Region) {
    fitToHeight(region)
    fitToWidth(region)
}
