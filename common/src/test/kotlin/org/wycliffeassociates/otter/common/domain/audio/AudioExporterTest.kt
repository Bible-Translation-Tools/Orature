package org.wycliffeassociates.otter.common.domain.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import java.io.File
import kotlin.io.path.createTempDirectory

class AudioExporterTest {

    @Test
    fun exportMp3() {
        val inputFile = File(javaClass.classLoader.getResource("mini-sample-audio.wav").file)
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
            .exportMp3(inputFile, outputDir, license, contributors)
            .blockingAwait()

        val outputFile = outputDir.listFiles().singleOrNull()
        assertNotNull(outputFile)

        val metadata = Mp3MetadataAccessor(outputFile!!)
        assertEquals(2, metadata.artists().size)
        assertEquals(license.url, metadata.getLegalInformationUrl())
    }
}