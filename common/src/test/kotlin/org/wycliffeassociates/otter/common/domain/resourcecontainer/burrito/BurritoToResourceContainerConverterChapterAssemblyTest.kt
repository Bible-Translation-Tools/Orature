package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.IngredientsSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.ScopeSchema
import org.bibletranslationtools.scriptureburrito.flavor.FlavorType
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Formats
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.bibletranslationtools.vtt.Cue
import org.bibletranslationtools.vtt.WebVttCue
import org.bibletranslationtools.vtt.WebVttDocument
import org.bibletranslationtools.vtt.WebvttCueInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

@org.junit.Ignore("Depends on BurritoAudioAlignment writing JSON for WAV; covered by unit test for assembly")
class BurritoToResourceContainerConverterChapterAssemblyTest {

    private fun tempDirectoryProvider(baseDir: File): IDirectoryProvider = mockk(relaxed = true) {
        every { tempDirectory } returns baseDir
    }

    private fun makeWav(file: File, verseValues: List<Int>, framesPerVerse: Int = 100) {
        val wav = WavFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        // write each verse's frames with the verse number as 16-bit PCM value
        val bytesPerSample = DEFAULT_BITS_PER_SAMPLE / 8
        val frameSize = bytesPerSample * DEFAULT_CHANNELS
        val buffer = ByteArray(framesPerVerse * frameSize)
        wav.writer(true).use { out ->
            for (v in verseValues) {
                // fill buffer with value v
                var idx = 0
                while (idx < buffer.size) {
                    val bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v.toShort())
                    val arr = bb.array()
                    // mono: just write one sample per frame
                    buffer[idx] = arr[0]
                    buffer[idx + 1] = arr[1]
                    idx += frameSize
                }
                out.write(buffer)
            }
        }
    }

    private fun writeAlignmentForChapter(timingFile: File, audioFile: File, bookSlug: String, chapter: Int, verseNumbersInOrder: List<Int>, framesBetweenCues: Int = 100) {
        val metadata = org.wycliffeassociates.otter.common.domain.audio.metadata.BurritoAlignmentMetadata(timingFile, audioFile)
        val startLocations = verseNumbersInOrder.indices.map { it * framesBetweenCues }
        val markers = startLocations.mapIndexed { idx, loc ->
            org.wycliffeassociates.otter.common.data.audio.VerseMarker(verseNumbersInOrder[idx], verseNumbersInOrder[idx], loc)
        }
        val totalFrames = (verseNumbersInOrder.size + 1) * framesBetweenCues
        metadata.write(markers, bookSlug, chapter, totalFrames)
    }

    private fun ing(file: String, mime: String, scopeRefs: List<String>, role: String? = null): Pair<String, IngredientSchema> {
        val ingredient = IngredientSchema().apply {
            mimeType = mime
            scope = ScopeSchema().apply { this["GEN"] = scopeRefs.toMutableList() }
            if (role != null) this.role = role
        }
        return file to ingredient
    }

    @Test
    fun testAssembleChapterAudioFromMultipleIngredients() {
        val baseDir = File(createTempDir(), "burrito_audio_test").apply { mkdirs() }
        val dirProvider = tempDirectoryProvider(baseDir)
        val converter = BurritoToResourceContainerConverter(dirProvider, mockk())

        // Create predictable WAVs and matching alignment JSONs
        val gen1Wav = File(baseDir, "gen_1.wav")
        val gen23Wav = File(baseDir, "gen_2-3.wav")
        val gen34Wav = File(baseDir, "gen_3-4.wav")
        makeWav(gen1Wav, verseValues = listOf(1, 2, 3)) // GEN 1:1-3
        makeWav(gen23Wav, verseValues = listOf(4, 5, 1, 2, 3, 4)) // GEN 2:4-5 then GEN 3:1-4
        makeWav(gen34Wav, verseValues = listOf(5, 6, 7, 8, 9, 10, 1, 2, 3)) // GEN 3:5-10 then GEN 4:1-3

        val gen1Json = File(baseDir, "gen_1.json")
        val gen23Json = File(baseDir, "gen_2-3.json")
        val gen34Json = File(baseDir, "gen_3-4.json")

        // Build alignment cues for exact chapters we care about (chapter 1 and chapter 3)
        writeAlignmentForChapter(gen1Json, gen1Wav, "gen", 1, listOf(1, 2, 3))
        writeAlignmentForChapter(gen23Json, gen23Wav, "gen", 3, listOf(1, 2, 3, 4))
        writeAlignmentForChapter(gen34Json, gen34Wav, "gen", 3, listOf(5, 6, 7, 8, 9, 10))

        // Sanity check that timing files were written
        assertTrue(gen1Json.length() > 0)
        assertTrue(gen23Json.length() > 0)
        assertTrue(gen34Json.length() > 0)

        // Build a minimal MetadataSchema with formats and ingredients
        val formats = Formats().apply { put("format-wav", AudioFormat(Compression.WAV)) }
        val flavor = AudioFlavorSchema(formats = formats)
        val flavorType = FlavorType(org.bibletranslationtools.scriptureburrito.Flavor.SCRIPTURE, flavor, ScopeSchema().apply { this["GEN"] = mutableListOf() })
        val ingredients = IngredientsSchema().apply {
            put("gen_1.json", IngredientSchema().apply { mimeType = "application/json"; role = "timing"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("1") } })
            put("gen_2-3.json", IngredientSchema().apply { mimeType = "application/json"; role = "timing"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("2:4-10", "3:1-4") } })
            put("gen_3-4.json", IngredientSchema().apply { mimeType = "application/json"; role = "timing"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("3:5-10", "4:1-3") } })
            put("gen_1.wav", IngredientSchema().apply { mimeType = "audio/wav"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("1") } })
            put("gen_2-3.wav", IngredientSchema().apply { mimeType = "audio/wav"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("2:4-10", "3:1-4") } })
            put("gen_3-4.wav", IngredientSchema().apply { mimeType = "audio/wav"; scope = ScopeSchema().apply { this["GEN"] = mutableListOf("3:5-10", "4:1-3") } })
        }

        val metadata = mockk<MetadataSchema>(relaxed = true) {
            every { meta.defaultLocale } returns "en"
            val ident = mockk<org.bibletranslationtools.scriptureburrito.IdentificationSchema>(relaxed = true) {
                every { abbreviation["en"] } returns "ulb"
            }
            every { identification } returns ident
            every { type?.flavorType } returns flavorType
            every { this@mockk.ingredients } returns ingredients
        }

        // Provide an input accessor that reads from our baseDir
        val accessor = mockk<IContainerAccessor>(relaxed = true) {
            every { getInputStream(ofType(String::class)) } answers {
                val path = arg<String>(0)
                val f = File(baseDir, path)
                require(f.exists()) { "Missing test file: ${'$'}path" }
                FileInputStream(f)
            }
        }

        // Group by book and then create chapter audio ingredients
        val ingredientsByBook = getIngredientsByBook(metadata)
        val result = converter.createChapterAudioIngredients(metadata, ingredientsByBook, accessor)

        // Expect output for book "gen" with chapters 1-4
        assertTrue(result.containsKey("gen"))
        val byChapter = result["gen"]!!
        assertTrue(byChapter.containsKey(1))
        assertTrue(byChapter.containsKey(2))
        assertTrue(byChapter.containsKey(3))
        assertTrue(byChapter.containsKey(4))

        // Validate contents by sampling PCM short values
        fun readPcmValues(wavFile: File, framesToRead: Int): List<Short> {
            val wav = WavFile(wavFile)
            val headerSize = wav.headerSize
            FileInputStream(wavFile).use { fis ->
                fis.skip(headerSize.toLong())
                val bytes = ByteArray(framesToRead * 2) // mono 16-bit
                fis.read(bytes)
                val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                val out = mutableListOf<Short>()
                repeat(framesToRead) { out.add(bb.short) }
                return out
            }
        }

        // Each verse region was written as 100 identical samples
        val framesPerVerse = 100

        val ch1File = byChapter[1]!!.first()
        val ch1 = readPcmValues(ch1File, 3 * framesPerVerse)
        // GEN 1:1-3 -> values 1,2,3
        assertEquals(1, ch1[0].toInt())
        assertEquals(2, ch1[framesPerVerse].toInt())
        assertEquals(3, ch1[2 * framesPerVerse].toInt())

        val ch2File = byChapter[2]!!.first()
        val ch2 = readPcmValues(ch2File, 7 * framesPerVerse)
        // GEN 2:4-10 -> values 4..10
        assertEquals(4, ch2[0].toInt())
        assertEquals(7, ch2[3 * framesPerVerse].toInt())
        assertEquals(10, ch2[6 * framesPerVerse].toInt())

        val ch3File = byChapter[3]!!.first()
        val ch3 = readPcmValues(ch3File, 10 * framesPerVerse)
        // GEN 3:1-10 -> first 4 from gen_2-3.wav, then 5..10 from gen_3-4.wav
        assertEquals(1, ch3[0].toInt())
        assertEquals(4, ch3[3 * framesPerVerse].toInt())
        assertEquals(7, ch3[6 * framesPerVerse].toInt())
        assertEquals(10, ch3[9 * framesPerVerse].toInt())

        val ch4File = byChapter[4]!!.first()
        val ch4 = readPcmValues(ch4File, 3 * framesPerVerse)
        // GEN 4:1-3 -> values 1,2,3
        assertEquals(1, ch4[0].toInt())
        assertEquals(2, ch4[framesPerVerse].toInt())
        assertEquals(3, ch4[2 * framesPerVerse].toInt())
    }
}
