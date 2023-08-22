package org.wycliffeassociates.otter.common.domain.audio

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.UnknownMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
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
            else -> OratureCueType.UNKNOWN
        }
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
}

internal class OratureMarkers {
    private val cueMap: MutableMap<OratureCueType, MutableList<AudioMarker>> = mutableMapOf(
        OratureCueType.CHUNK to mutableListOf(),
        OratureCueType.VERSE to mutableListOf(),
        OratureCueType.CHAPTER_TITLE to mutableListOf(),
        OratureCueType.BOOK_TITLE to mutableListOf(),
        OratureCueType.LICENSE to mutableListOf(),
    )

    fun getCues(): List<AudioCue> {
        return cueMap.values.flatMap { it.map { it.toCue() } }
    }

    @Synchronized
    fun getMarkers(type: OratureCueType): List<AudioMarker> {
        if (!cueMap.containsKey(type)) cueMap[type] = mutableListOf()
        return cueMap[type]!!
    }

    fun addMarkers(type: OratureCueType, markers: List<AudioMarker>) {
        if (!cueMap.containsKey(type)) cueMap[type] = mutableListOf()
        cueMap[type]!!.addAll(markers)
    }

    fun addMarker(type: OratureCueType, marker: AudioMarker) {
        if (!cueMap.containsKey(type)) cueMap[type] = mutableListOf()
        cueMap[type]!!.add(marker)
    }

    fun clearMarkersOfType(type: OratureCueType) {
        if (!cueMap.containsKey(type)) {
            cueMap[type] = mutableListOf()
            return
        } else {
            cueMap[type]!!.clear()
        }
    }

    private fun addEntry(entry: Map.Entry<OratureCueType, MutableList<AudioMarker>>) {
        if (!cueMap.containsKey(entry.key)) cueMap[entry.key] = mutableListOf()
        cueMap[entry.key]!!.addAll(entry.value)
    }

    /**
     * Deep copies markers into a new instance of OratureMarkers
     */
    fun copy(): OratureMarkers {
        val newCopy = OratureMarkers()
        cueMap.forEach { newCopy.addEntry(it) }
        return newCopy
    }

    /**
     * Copies all markers from the provided markers to the internal map of markers
     *
     * @param markers the markers to copy from
     */
    fun import(markers: OratureMarkers) {
        markers.cueMap.entries.forEach {
            addEntry(it)
        }
    }
}