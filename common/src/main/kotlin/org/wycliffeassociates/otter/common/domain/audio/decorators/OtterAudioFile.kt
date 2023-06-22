package org.wycliffeassociates.otter.common.domain.audio.decorators

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.*
import java.io.File
import java.util.regex.Pattern

class OratureAudioFile : AudioFile {

    val logger = LoggerFactory.getLogger(OratureAudioFile::class.java)

    private val cues: List<AudioCue> = mutableListOf()
    private val extraCues: List<AudioCue> = mutableListOf()
    private val cueParser = OratureCueParser(this)

    private val markers = OratureMarkers()

    private fun initializeCues() {
        val allCues = metadata.getCues()
        separateOratureCues(allCues)
        metadata.clearMarkers()
        cues.forEach {
            metadata.addCue(it.location, it.label)
        }
        extraCues.forEach {
            metadata.addCue(it.location, it.label)
        }
        markers.import(cueParser.parse())
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
        return cues
    }

    fun getMarker(type: OratureCueType): List<AudioMarker> {
        return getCuesFromMap(type)
    }

    fun addCues(cues: List<AudioCue>) {
        cues.forEach {
            addCue(it.location, it.label)
        }
    }

    fun addVerseMarker(marker: VerseMarker) {
        markers.addMarker(OratureCueType.VERSE, marker)
    }

    fun addChunkMarker(marker: ChunkMarker) {
        markers.addMarker(OratureCueType.CHUNK, marker)
    }

    override fun addCue(location: Int, label: String) {
        super.addCue(location, label)
        cues as MutableList
        cues.add(AudioCue(location, label))
        metadata.addCue(location, label)
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
        cues as MutableList
        cues.clear()
        clearCuesFromMap(OratureCueType.VERSE)
    }

    override fun update() {
        metadata.clearMarkers()
        extraCues.forEach {
            metadata.addCue(it)
        }
        getCuesFromMap(OratureCueType.VERSE).forEach {
            metadata.addCue(it.toCue())
        }
        super.update()
    }

    private fun separateOratureCues(allCues: List<AudioCue>) {
        val oratureRegex = Regex("^orature-vm-((\\d+)(?:-(\\d+))?)\$")
        val loneDigitRegex = Regex("^((\\d+)(?:-(\\d+))?)$")
        val numberRegex = Regex("((\\d+)(?:-(\\d+))?)")

        val oratureCues = allCues.filter { it.label.matches(oratureRegex) }
        val leftoverCues = allCues.filter { !oratureCues.contains(it) }
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
            addMatchingCues(oratureCues, oratureRegex)
        } else if (loneDigits.isNotEmpty()) {
            addMatchingCues(loneDigits.map { AudioCue(it.location, it.label.trim()) }, loneDigitRegex)
        } else if (potentialCues.isNotEmpty()) {
            addMatchingCues(potentialCues, numberRegex)
        }
        extraCues as MutableList
        extraCues.addAll(leftoverCues)
    }

    private fun addMatchingCues(baseCueList: List<AudioCue>, regex: Regex) {
        cues as MutableList
        val mapped = baseCueList.map {
            val match = regex.find(it.label)
            val groups = match!!.groupValues
            val label = if (groups.size > 1) {
                match!!.groupValues[1]!!
            } else {
                match!!.groupValues.first()!!
            }
            AudioCue(it.location, "orature-vm-${label}")
        }
        cues.addAll(mapped)
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

class UnknownMarker(override val location: Int, override val label: String): AudioMarker {
    constructor(cue: AudioCue): this(cue.location, cue.label)
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

class OratureCueParser(val audio: OratureAudioFile) {

    private val parsers = listOf(
        VerseMarkerParser(),
        ChunkMarkerParser()
    )

    fun parse(): OratureMarkers {
        val markers = OratureMarkers()

        val cues = audio.getCues()
        var result = parsers[0].parse(cues)
        for (i in 1..parsers.size) {
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

    fun match
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