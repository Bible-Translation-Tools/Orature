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
package org.wycliffeassociates.otter.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.pcm.PcmOutputStream
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDatabaseAccessors
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.*
import java.io.File
import java.time.LocalDate
import java.util.*

/**
 * Applies the given transformation to each element in the keyset of the map, and uses [doAssertEquals]
 * to compare the result of each transform with the corresponding expected value in the valueset.
 *
 * This assumes that the map keys represent the input to a test function, and the values represent the
 * corresponding expected output.
 *
 * @param transform The transformation to apply to each key in the keyset. This is the function under test.
 */
fun <T, V> Map<T, V>.assertEqualsForEach(transform: (T) -> V) {
    var allPassed = true
    forEach {
        val output = transform(it.key)
        val expected = it.value
        allPassed = doAssertEquals(expected, output) && allPassed
    }
    if (!allPassed) throw AssertionError()
}
/**
 * Wrapper function for [org.junit.Assert.assertEquals] that catches any thrown [AssertionError].
 * If an error is caught, the expected output and actual output will be printed
 *
 * @return The boolean result of the comparison
 */
fun <T> doAssertEquals(expected: T, output: T): Boolean {
    return try {
        Assert.assertEquals(expected, output)
        true
    } catch (e: AssertionError) {
        println("Expected: $expected, Output: $output")
        false
    }
}

fun createTestWavFile(dir: File): File {
    val testFile = dir.resolve("test-take-${Date().time}.wav")
        .apply { createNewFile(); deleteOnExit() }

    val wav = WavFile(
        testFile,
        DEFAULT_CHANNELS,
        DEFAULT_SAMPLE_RATE,
        DEFAULT_BITS_PER_SAMPLE,
        WavMetadata(listOf(CueChunk()))
    )
    WavOutputStream(wav).use {
        for (i in 0 until 4) {
            it.write(i)
        }
    }
    wav.update()
    return testFile
}

fun createTestChapterRepresentationFiles(dir: File): List<File> {
    val pcmFile = dir.resolve(CHAPTER_NARRATION_FILE_NAME)
        .apply { createNewFile(); deleteOnExit() }

    val pcm = PcmFile(pcmFile)
    PcmOutputStream(pcm).use {
        for (i in 0 .. 10) {
            it.write(i)
        }
    }

    val jsonFile = dir.resolve(ACTIVE_VERSES_FILE_NAME)
        .apply { createNewFile(); deleteOnExit() }

    val jsonData = "[{\"placed\":true,\"marker\":{\"type\":\"BookMarker\",\"bookSlug\":\"jhn\",\"location\":0}" +
            ",\"sectors\":[{\"start\":0,\"end\":2}]},{\"placed\":true,\"marker\":{\"type\":\"ChapterMarker\"," +
            "\"chapterNumber\":1,\"location\":0},\"sectors\":[{\"start\":3,\"end\":6}]},{\"placed\":true" +
            ",\"marker\":{\"type\":\"VerseMarker\",\"start\":1,\"end\":1,\"location\":0}" +
            ",\"sectors\":[{\"start\":7,\"end\":10}]}]"

    jsonFile.bufferedWriter().use {
        it.write(jsonData)
    }

    return listOf(pcmFile, jsonFile)
}

fun createTestActiveVersesFile(dir: File, fileName: String = ACTIVE_VERSES_FILE_NAME): File {
    val testFile = dir.resolve(fileName)
        .apply { createNewFile(); deleteOnExit() }

    val testData = "[{\"placed\":true,\"marker\":{\"type\":\"BookMarker\",\"bookSlug\":\"jhn\",\"location\":0}" +
            ",\"sectors\":[{\"start\":0,\"end\":2}]},{\"placed\":true,\"marker\":{\"type\":\"ChapterMarker\"," +
            "\"chapterNumber\":1,\"location\":0},\"sectors\":[{\"start\":3,\"end\":6}]},{\"placed\":true" +
            ",\"marker\":{\"type\":\"VerseMarker\",\"start\":1,\"end\":1,\"location\":0}" +
            ",\"sectors\":[{\"start\":7,\"end\":1}]}]"

    testFile.bufferedWriter().use {
        it.write(testData)
    }
    return testFile
}

fun getDublinCore(resource: ResourceMetadata): DublinCore {
    return dublincore {
        conformsTo = "0.2"
        identifier = resource.identifier
        issued = LocalDate.now().toString()
        modified = LocalDate.now().toString()
        language = language {
            identifier = resource.language.slug
            direction = resource.language.direction
            title = resource.language.name
        }
        creator = "Orature"
        version = resource.version
        rights = resource.license
        format = MimeType.of(resource.format).norm
        subject = resource.subject
        type = resource.type.slug
        title = resource.title
    }
}

fun getResourceMetadata(language: Language): ResourceMetadata {
    return ResourceMetadata(
        conformsTo = "rc0.2",
        creator = "Door43 World Missions Community",
        description = "Description",
        format = "text/usfm",
        identifier = "ulb",
        issued = LocalDate.now(),
        language = language,
        modified = LocalDate.now(),
        publisher = "unfoldingWord",
        subject = "Bible",
        type = ContainerType.Bundle,
        title = "Unlocked Literal Bible",
        version = "1",
        license = "",
        path = File(".")
    )
}

fun getEnglishLanguage(id: Int): Language {
    return Language(
        "en",
        "English",
        "English",
        "ltr",
        isGateway = true,
        region = "Europe",
        id = id
    )
}

fun getSpanishLanguage(id: Int = 0): Language {
    return Language(
        "es",
        "Spanish",
        "Spanish",
        "ltr",
        isGateway = false,
        region = "Europe",
        id = id
    )
}

fun getGenesisCollection(): Collection {
    return Collection(
        sort = 1,
        slug = "gen",
        labelKey = "project",
        titleKey = "Genesis",
        resourceContainer = null
    )
}

fun buildWorkbook(
    directoryProvider: IDirectoryProvider,
    db: IWorkbookDatabaseAccessors,
    source: Collection,
    target: Collection
) = WorkbookRepository(
    directoryProvider,
    db
).get(source, target)

fun createWavFile(dir: File, name: String, data: ByteArray): File {
    val file = File(dir, name)
    val oratureAudioFile = OratureAudioFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
    oratureAudioFile.writer().use { os ->
        os.write(data)
    }
    return file
}

fun readTextFromAudioFile(oratureAudioFile: OratureAudioFile, bufferSize: Int): String {
    val reader = oratureAudioFile.reader()
    val buffer = ByteArray(bufferSize)
    reader.open()
    var outStr = ""
    while (reader.hasRemaining()) {
        reader.getPcmBuffer(buffer)
        outStr = buffer.decodeToString()
    }
    return outStr
}

fun templateAudioFileName(
    language: String,
    resource: String,
    project: String,
    chapterLabel: String,
    extension: String? = null
): String {
    val ext = extension ?: ""
    return "${language}_${resource}_${project}_c${chapterLabel}$ext"
}

fun createTestRc(
    dir: File,
    dublinCore: DublinCore,
    sourceFiles: List<File> = listOf()
): ResourceContainer {
    return ResourceContainer.create(dir) {
        manifest = Manifest(dublinCore, listOf(), Checking())

        sourceFiles.forEach {
            addFileToContainer(it, "${RcConstants.SOURCE_AUDIO_DIR}/${it.name}")
        }

        write()
    }
}

fun readChunksFile(projectDir: File): Map<Int, List<Content>> {
    val chunkFile = File(projectDir, RcConstants.CHUNKS_FILE)
    val factory = JsonFactory()
    factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
    val mapper = ObjectMapper(factory)
    mapper.registerKotlinModule()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

    val chunks = mutableMapOf<Int, List<Content>>()
    val typeRef: TypeReference<HashMap<Int, List<Content>>> =
        object : TypeReference<HashMap<Int, List<Content>>>() {}
    val map: Map<Int, List<Content>> = mapper.readValue(chunkFile, typeRef)
    chunks.putAll(map)
    return chunks
}
