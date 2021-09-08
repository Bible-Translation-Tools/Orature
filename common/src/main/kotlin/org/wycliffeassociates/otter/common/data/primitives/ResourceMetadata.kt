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

import java.io.File
import java.time.LocalDate

data class ResourceMetadata(
    val conformsTo: String,
    val creator: String,
    val description: String,
    val format: String,
    val identifier: String,
    val issued: LocalDate,
    val language: Language,
    val modified: LocalDate,
    val publisher: String,
    val subject: String,
    val type: ContainerType,
    val title: String,
    val version: String,
    val license: String,
    val path: File,
    val id: Int = 0
)
