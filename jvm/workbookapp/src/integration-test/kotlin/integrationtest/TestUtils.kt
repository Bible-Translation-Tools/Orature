/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package integrationtest

import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File
import java.time.LocalDate
import java.util.*

val enUlbTestMetadata =
    ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "text/usfm",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true, ""),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        "",
        File("."),
    )

fun createTestWavFile(dir: File): File {
    val testFile =
        dir.resolve("test-take-${Date().time}.wav")
            .apply {
                createNewFile()
                deleteOnExit()
            }

    val wav =
        WavFile(
            testFile,
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE,
            WavMetadata(listOf(CueChunk())),
        )
    WavOutputStream(wav).use {
        for (i in 0 until 4) {
            it.write(i)
        }
    }
    wav.update()
    return testFile
}
