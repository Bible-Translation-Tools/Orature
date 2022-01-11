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
package org.wycliffeassociates.otter.common.domain.mapper

import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import java.io.File
import java.time.LocalDate

fun DublinCore.mapToMetadata(dir: File, lang: Language): ResourceMetadata {
    val (issuedDate, modifiedDate) = listOf(issued, modified)
        .map {
            // String could be in any of [W3 ISO8601 profile](https://www.w3.org/TR/NOTE-datetime)
            // Sanitize to be YYYY-MM-DD
            it
                // Remove any time information
                .substringBefore("T")
                // Split into YYYY, MM, and DD parts
                .split("-")
                .toMutableList()
                // Add any months or days to complete the YYYY-MM-DD format
                .apply {
                    for (i in 1..(3 - size)) {
                        add("01")
                    }
                }
                // Combine back to a string
                .joinToString("-")
                // Parse to local date
                .let { sanitized -> LocalDate.parse(sanitized) }
        }

    return ResourceMetadata(
        conformsTo,
        creator,
        description,
        format,
        identifier,
        issuedDate,
        lang,
        modifiedDate,
        publisher,
        subject,
        ContainerType.of(type),
        title,
        version,
        rights,
        dir
    )
}
