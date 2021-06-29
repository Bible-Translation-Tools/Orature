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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities

data class ResourceMetadataEntity(
    var id: Int,
    var conformsTo: String,
    var creator: String,
    var description: String,
    var format: String,
    var identifier: String,
    var issued: String,
    var languageFk: Int,
    var modified: String,
    var publisher: String,
    var subject: String,
    var type: String,
    var title: String,
    var version: String,
    var path: String,
    var derivedFromFk: Int?
)