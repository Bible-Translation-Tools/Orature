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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import java.io.File
import java.time.LocalDate
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.Marker
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.Take

object TestDataStore {
    val languages = listOf(
            Language("ar", "العَرَبِيَّة", "Arabic", "rtl", true, "Africa"),
            Language("en", "English", "English", "ltr", true, "Africa"),
            Language("atj", "Atikamekw", "Atikamekw", "ltr", false, "Africa"),
            Language("bbs", "Bakpinka", "Bakpinka", "ltr", false, "Africa"),
            Language("nfl", "Äiwoo", "Ayiwo", "ltr", false, "Africa")
    )
    val resourceContainers = listOf(
            ResourceMetadata(
                    "rc0.2",
                    "Someone or Organization",
                    "One or two sentence description of the resource.",
                    "text/usfm",
                    "ulb",
                    LocalDate.now(),
                    languages.first(), // no id initially set!
                    LocalDate.now(),
                    "Name of Publisher",
                    "Bible",
                    ContainerType.Book,
                    "Unlocked Literal Bible",
                    "3",
                    "",
                    File("/path/to/my/container")
            ),
            ResourceMetadata(
                    "rc0.2",
                    "J.R.R. Tolkien",
                    "An epic masterpiece of fiction.",
                    "text/usfm",
                    "lotr",
                    LocalDate.now(),
                    languages[1], // no id initially set!
                    LocalDate.now(),
                    "Allen & Unwin",
                    "Fiction",
                    ContainerType.Book,
                    "The Lord of the Rings",
                    "1",
                    "",
                    File("/path/to/my/amazing/esource")
            )
    )

    val collections = listOf(
            Collection(
                    1,
                    "rom",
                    "book",
                    "romans",
                    resourceContainers.first()
            ),
            Collection(
                    2,
                    "bible-ot",
                    "anthology",
                    "old_testament",
                    resourceContainers.last()
            ),
            Collection(
                    3,
                    "bible-nt",
                    "anthology",
                    "new_testament",
                    resourceContainers.last()
            )
    )

    val markers = listOf(
            Marker(
                    3,
                    2030,
                    "verse3"
            ),
            Marker(
                    45,
                    948163,
                    "verse45"
            ),
            Marker(
                    5,
                    58723,
                    "note5"
            ),
            Marker(
                    12,
                    46123,
                    "verse12"
            )
    )

    val takes = listOf(
            Take(
                    filename = "take1.wav",
                    path = File("take1.wav"),
                    number = 1,
                    created = LocalDate.now(),
                    deleted = null,
                    played = false,
                    markers = markers.subList(0, 1)
            ),
            Take(
                    filename = "take2.wav",
                    path = File("take2.wav"),
                    number = 2,
                    created = LocalDate.now(),
                    deleted = null,
                    played = true,
                    markers = markers.subList(2, 3)
            )
    )

    val content = listOf(
            Content(
                0,
                "verse1",
                1,
                1,
                null,
                null,
                null,
                ContentType.TEXT,
                1
            ),
            Content(
                41,
                "verse42",
                42,
                42,
                takes.first(),
                null,
                null,
                ContentType.TEXT,
                1
            )
    )
}
