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
        dir
    )
}
