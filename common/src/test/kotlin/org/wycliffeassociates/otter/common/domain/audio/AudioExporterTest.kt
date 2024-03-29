/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.audio

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import java.io.File
import kotlin.io.path.createTempDirectory
import org.wycliffeassociates.otter.common.createTestWavFile

class AudioExporterTest {

    @Test
    fun exportMp3() {

        val inputFile = createTestWavFile(File("./"))
            .apply { deleteOnExit() }
        val outputDir = createTempDirectory().toFile()
            .apply { deleteOnExit() }

        val license = License.CCBYSA4_0
        val contributors = listOf(
            Contributor("Test User 1"),
            Contributor("Test User 2")
        )

        AudioExporter()
            .apply {
                audioConverter = AudioConverter()
            }
            .exportMp3(inputFile, outputDir, AudioExporter.ExportMetadata(license, contributors))
            .blockingAwait()

        val outputFile = outputDir.resolve(inputFile.nameWithoutExtension + ".mp3")
        assertTrue(outputFile.exists())

        val oratureAudioFile = OratureAudioFile(outputFile)
        assertEquals(2, oratureAudioFile.metadata.artists().size)
        assertEquals(license.url, oratureAudioFile.metadata.getLegalInformationUrl())
    }
}
