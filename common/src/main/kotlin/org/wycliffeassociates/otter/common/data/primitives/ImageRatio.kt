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
package org.wycliffeassociates.otter.common.data.primitives

enum class ImageRatio(val width: Int, val height: Int) {
    SIXTEEN_BY_NINE(16, 9),
    FOUR_BY_THREE(4, 3),
    FOUR_BY_ONE(4, 1),
    TWO_BY_ONE(2, 1),
    ONE_BY_ONE(1, 1),
    DEFAULT(0, 0);

    override fun toString(): String {
        return if (this != DEFAULT) {
            "${width}x$height"
        } else ""
    }

    fun getImageSuffix(): String {
        return if (this != DEFAULT) {
            "_${toString()}"
        } else ""
    }
}