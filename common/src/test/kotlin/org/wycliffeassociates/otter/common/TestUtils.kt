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

import com.nhaarman.mockitokotlin2.mock
import java.io.File
import java.util.*
import org.junit.Assert
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.dublincore
import org.wycliffeassociates.resourcecontainer.entity.language
import java.time.LocalDate

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

fun getResourceMetadata(langauge: Language): ResourceMetadata {
    return ResourceMetadata(
        conformsTo = "rc0.2",
        creator = "Door43 World Missions Community",
        description = "Description",
        format = "text/usfm",
        identifier = "ulb",
        issued = LocalDate.now(),
        language = langauge,
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
    db: WorkbookRepository.IDatabaseAccessors,
    source: Collection,
    target: Collection
) = WorkbookRepository(
    mock(),
    db
).get(source, target)

fun createWavFile(dir: File, name: String, data: ByteArray): File {
    val file = File(dir, name)
    val audioFile = AudioFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
    audioFile.writer().use { os ->
        os.write(data)
    }
    return file
}

fun readTextFromAudioFile(audioFile: AudioFile, bufferSize: Int): String {
    val reader = audioFile.reader()
    val buffer = ByteArray(bufferSize)
    reader.open()
    var outStr = ""
    while (reader.hasRemaining()) {
        reader.getPcmBuffer(buffer)
        outStr = buffer.decodeToString()
    }
    return outStr
}
