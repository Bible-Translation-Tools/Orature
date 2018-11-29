package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import java.io.File
import java.time.LocalDate

object TestDataStore {
    val languages = listOf(
            Language("ar", "العَرَبِيَّة", "Arabic", "rtl", true),
            Language("en", "English", "English", "ltr", true),
            Language("atj", "Atikamekw", "Atikamekw", "ltr", false),
            Language("bbs", "Bakpinka", "Bakpinka", "ltr", false),
            Language("nfl", "Äiwoo", "Ayiwo", "ltr", false)
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
                    "book",
                    "Unlocked Literal Bible",
                    "3",
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
                    "book",
                    "The Lord of the Rings",
                    "1",
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
                    "take1.wav",
                    File("take1.wav"),
                    1,
                    LocalDate.now(),
                    false,
                    markers.subList(0, 1)
            ),
            Take(
                    "take2.wav",
                    File("take2.wav"),
                    2,
                    LocalDate.now(),
                    true,
                    markers.subList(2, 3)
            )
    )

    val content = listOf(
            Content(
                    0,
                    "verse1",
                    1,
                    1,
                    null
            ),
            Content(
                    41,
                    "verse42",
                    42,
                    42,
                    takes.first()
            )
    )
}