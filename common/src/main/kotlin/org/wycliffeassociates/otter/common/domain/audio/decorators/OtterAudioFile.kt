package org.wycliffeassociates.otter.common.domain.audio.decorators

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.*
import java.io.File
import java.lang.UnsupportedOperationException
import java.util.regex.Pattern

class OratureAudioFile : AudioFile {

    val logger = LoggerFactory.getLogger(OratureAudioFile::class.java)

    private val extraCues: List<AudioCue> = mutableListOf()

    private val markers = OratureMarkers()

    private fun initializeCues() {
        markers.import(OratureCueParser.parse(this))
        logger.info("Parsed ${getCuesFromMap(OratureCueType.VERSE)} verses")
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

    fun addCues(cues: List<AudioCue>) {

    }

    fun addVerseMarker(marker: VerseMarker) {
        markers.addMarker(OratureCueType.VERSE, marker)
    }

    fun addChunkMarker(marker: ChunkMarker) {
        markers.addMarker(OratureCueType.CHUNK, marker)
    }

    fun importMetadata(metadata: AudioMetadata) {
        markers.import(OratureCueParser.parse(metadata))
    }

    fun importCues(cues: List<AudioCue>) {
        markers.import(OratureCueParser.parse(cues))
    }

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
            addVerseMarker(marker)
        }
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

    private fun clearCuesFromMap(type: OratureCueType) {
        markers.clearMarkersOfType(type)
    }
}

interface AudioMarker {
    val label: String
    val location: Int

    fun formatMarkerText(): String

    fun toCue(): AudioCue {
        return AudioCue(location, formatMarkerText())
    }
}

class UnknownMarker(override val location: Int, override val label: String) : AudioMarker {
    constructor(cue: AudioCue) : this(cue.location, cue.label)

    override fun formatMarkerText() = label
}

class VerseMarker(val start: Int, val end: Int, override val location: Int) : AudioMarker {
    override val label: String
        get() = if (end != start) "$start-$end" else "$start"

    override fun formatMarkerText() = "orature-vm-${label}"
}

class ChunkMarker(val chunk: Int, override val location: Int) : AudioMarker {
    override val label = "$chunk"
    override fun formatMarkerText() = "orature-chunk-${label}"
}

enum class OratureCueType {
    UNKNOWN,
    CHUNK,
    VERSE,
    CHAPTER_TITLE,
    BOOK_TITLE,
    LICENSE
}

class OratureMarkers {
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

object OratureCueParser {

    private val parsers = listOf(
        VerseMarkerParser(),
        ChunkMarkerParser()
    )

    fun parse(metadata: AudioMetadata): OratureMarkers {
        return parse(metadata.getCues())
    }

    fun parse(audio: AudioFile): OratureMarkers {
        return parse(audio.metadata)
    }

    fun parse(cues: List<AudioCue>): OratureMarkers {
        val markers = OratureMarkers()

        var result = parsers[0].parse(cues)
        for (i in 1 until parsers.size) {
            markers.addMarkers(parsers[i - 1].cueType, result.accepted)
            result = parsers[i].parse(result.rejected)
        }
        markers.addMarkers(OratureCueType.UNKNOWN, result.rejected.map { UnknownMarker(it) })

        return markers
    }
}

class MarkerParseResult(val accepted: List<AudioMarker>, val rejected: List<AudioCue>)

interface MarkerParser {
    val cueType: OratureCueType
    val pattern: Pattern

    fun match(cue: AudioCue): AudioMarker?

    fun parse(cues: List<AudioCue>): MarkerParseResult {
        val accepted = mutableListOf<AudioMarker>()
        val rejected = mutableListOf<AudioCue>()

        cues.forEach { cue ->
            val result = match(cue)
            if (result != null) {
                accepted.add(result)
            } else {
                rejected.add(cue)
            }
        }

        return MarkerParseResult(accepted, rejected)
    }
}

class VerseMarkerParser : MarkerParser {
    override val cueType = OratureCueType.VERSE

    override val pattern: Pattern = Pattern.compile("^orature-vm-(\\d+)(?:-(\\d+))?\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val start: Int
        val end: Int
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            start = matcher.group(1).toInt()
            end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                matcher.group(2).toInt()
            } else start
            return VerseMarker(start, end, cue.location)
        }
        return null
    }

    override fun parse(cues: List<AudioCue>): MarkerParseResult {
        val accepted = mutableListOf<AudioMarker>()
        val rejected = mutableListOf<AudioCue>()

        val oratureCues: Pattern = Pattern.compile("^orature-*\$")
        val hasOratureCues = cues.any { oratureCues.matcher(it.label).matches() }

        if (hasOratureCues) {
            cues.forEach { cue ->
                val result = match(cue)
                if (result != null) {
                    accepted.add(result)
                } else {
                    rejected.add(cue)
                }
            }
            return MarkerParseResult(accepted, rejected)
        } else {
            matchAlternativeCues(cues, accepted, rejected)
        }

        return MarkerParseResult(accepted, rejected)
    }

    private fun matchAlternativeCues(
        cues: List<AudioCue>,
        accepted: MutableList<AudioMarker>,
        rejected: MutableList<AudioCue>
    ) {
        val oratureRegex = Regex("^orature-vm-(\\d+)(?:-(\\d+))?\$")

        val loneDigitRegex = Regex("^(\\d+)(?:-(\\d+))?$")
        val numberRegex = Regex("(\\d+)(?:-(\\d+))?")

        val oratureCues = cues.filter { it.label.matches(oratureRegex) }
        val leftoverCues = cues.filter { !oratureCues.contains(it) }
        val loneDigits = leftoverCues.filter { it.label.trim().matches(loneDigitRegex) }
        val potentialCues = leftoverCues
            .filter { !loneDigits.contains(it) }
            .filter { numberRegex.containsMatchIn(it.label) }
            .map {
                val match = numberRegex.find(it.label)
                val label = match!!.groupValues.first()!!
                AudioCue(it.location, label)
            }

        if (oratureCues.isNotEmpty()) {
            addMatchingCues(oratureCues, oratureRegex.toPattern(), accepted)
        } else if (loneDigits.isNotEmpty()) {
            addMatchingCues(
                loneDigits.map { AudioCue(it.location, it.label.trim()) },
                loneDigitRegex.toPattern(),
                accepted
            )
        } else if (potentialCues.isNotEmpty()) {
            addMatchingCues(potentialCues, numberRegex.toPattern(), accepted)
        }
        rejected.addAll(leftoverCues)
    }

    private fun addMatchingCues(cues: List<AudioCue>, pattern: Pattern, accepted: MutableList<AudioMarker>) {
        cues.forEach { cue ->
            val matcher = pattern.matcher(cue.label)
            if (matcher.matches()) {
                val start = matcher.group(1).toInt()
                val end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                    matcher.group(2).toInt()
                } else start
                accepted.add(VerseMarker(start, end, cue.location))
            }
        }
    }
}

class ChunkMarkerParser : MarkerParser {
    override val cueType = OratureCueType.CHUNK

    override val pattern: Pattern = Pattern.compile("^orature-chunk-(\\d+)(?:-(\\d+))?\$")

    override fun match(cue: AudioCue): AudioMarker? {
        val start: Int
        val end: Int
        val matcher = pattern.matcher(cue.label)
        if (matcher.matches()) {
            start = matcher.group(1).toInt()
            end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                matcher.group(2).toInt()
            } else start
            return VerseMarker(start, end, cue.location)
        }
        return null
    }
}