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

/**
 * Container type enum for [ResourceMetadata.type].
 *
 * See Resource Container [Container Types](https://resource-container.readthedocs.io/en/latest/container_types.html)
 */
enum class ContainerType(val slug: String) {
    Bundle("bundle"),
    Book("book"),
    Help("help"),

    @Deprecated("Type not supported")
    Dictionary("dict"),
    @Deprecated("Type not supported")
    Manual("man");

    companion object {
        private val map = values().associateBy { it.slug.toLowerCase() }

        /** @throws IllegalArgumentException */
        fun of(slug: String) =
            map[slug.toLowerCase()]
                ?: throw IllegalArgumentException("Container slug $slug not supported")
    }
}
