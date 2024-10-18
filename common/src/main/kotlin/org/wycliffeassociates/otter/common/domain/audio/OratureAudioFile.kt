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

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.UnknownMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.metadata.OratureMarkers
import java.io.File
import java.util.regex.Pattern
import kotlin.reflect.KClass

class OratureAudioFile : AudioFile {

    val logger = LoggerFactory.getLogger(OratureAudioFile::class.java)

    private val markers = OratureMarkers()

    private fun initializeCues() {
        markers.import(OratureCueParser.parse(this))
    }

    constructor() : super() {
        initializeCues()
    }

    constructor(file: File, metadata: AudioMetadata) : super(file, metadata) {
        initializeCues()
    }

    constructor(file: File) : super(file) {
        initializeCues()
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    ) : super(file, channels, sampleRate, bitsPerSample) {
        initializeCues()
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        metadata: AudioMetadata
    ) : super(file, channels, sampleRate, bitsPerSample, metadata) {
        initializeCues()
    }

    override fun getCues(): List<AudioCue> {
        return markers.getCues()
    }

    fun getMarker(type: OratureCueType): List<AudioMarker> {
        return getCuesFromMap(type)
    }

    fun getMarkerTypeFromClass(clazz: KClass<out AudioMarker>): OratureCueType {
        return when (clazz) {
            UnknownMarker::class -> OratureCueType.UNKNOWN
            VerseMarker::class -> OratureCueType.VERSE
            ChunkMarker::class -> OratureCueType.CHUNK
            ChapterMarker::class -> OratureCueType.CHAPTER_TITLE
            BookMarker::class -> OratureCueType.BOOK_TITLE
            else -> OratureCueType.UNKNOWN
        }
    }

    fun getVerseAndTitleMarkers(): List<AudioMarker> {
        return getMarker<BookMarker>()
            .plus(getMarker<ChapterMarker>())
            .plus(getMarker<VerseMarker>())
    }

    inline fun <reified T: AudioMarker> getMarker(): List<T> {
        val type = T::class
        val enum = getMarkerTypeFromClass(type)
        return getMarker(enum).map { it as T }
    }

    @Deprecated("Markers should be added to OtterAudioFile using typed addMarker methods")
    fun addCues(cues: List<AudioCue>) {
        markers.import(OratureCueParser.parse(cues))
    }

    inline fun <reified T: AudioMarker> addMarker(marker: T) {
        val enum = getMarkerTypeFromClass(T::class)
        addMarker(enum, marker)
    }

    fun addMarker(type: OratureCueType, marker: AudioMarker) {
        markers.addMarker(type, marker)
    }

    fun importMetadata(metadata: AudioMetadata) {
        markers.import(OratureCueParser.parse(metadata))
    }

    fun importCues(cues: List<AudioCue>) {
        markers.import(OratureCueParser.parse(cues))
    }

    @Deprecated("Markers should be added to OtterAudioFile using typed addMarker methods")
    override fun addCue(location: Int, label: String) {
        markers.import(OratureCueParser.parse(listOf(AudioCue(location, label))))
    }

    @Synchronized
    fun addVerseMarker(location: Int, label: String) {
        val regex = Pattern.compile("^(\\d+)(?:-(\\d+))?$")
        val match = regex.matcher(label.trim())
        if (match.matches()) {
            val verseStart = match.group(1).toInt()
            val verseEnd = match.group(2)?.toInt() ?: verseStart
            val marker = VerseMarker(verseStart, verseEnd, location)
            addMarker<VerseMarker>(marker)
        }
    }

    inline fun <reified T: AudioMarker> clearMarkersOfType() {
        val enum = getMarkerTypeFromClass(T::class)
        clearCuesFromMap(enum)
    }

    fun clearChunkMarkers() {
        clearCuesFromMap(OratureCueType.CHUNK)
    }

    fun clearVerseMarkers() {
        clearCuesFromMap(OratureCueType.VERSE)
    }

    override fun update() {
        metadata.clearMarkers()
        markers.getCues().forEach { metadata.addCue(it) }
        super.update()
    }

    private fun getCuesFromMap(type: OratureCueType): List<AudioMarker> {
        return markers.getMarkers(type)
    }

    fun clearCuesFromMap(type: OratureCueType) {
        markers.clearMarkersOfType(type)
    }

    fun clearMarkers() {
        OratureCueType.entries.forEach {
            markers.clearMarkersOfType(it)
        }
    }

    fun getMarkers(): List<AudioMarker> {
        return markers.getMarkers()
    }
}