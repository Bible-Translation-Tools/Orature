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
package org.wycliffeassociates.otter.common.data.primitives

import com.squareup.moshi.Json

data class Language(
    @Json(name = "lc")
    var slug: String,
    @Json(name = "ln")
    var name: String,
    @Json(name = "ang")
    var anglicizedName: String,
    @Json(name = "ld")
    var direction: String,
    @Json(name = "gw")
    var isGateway: Boolean,
    @Json(name = "lr")
    var region: String,
    var id: Int = 0
)
