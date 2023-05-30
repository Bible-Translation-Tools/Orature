package org.wycliffeassociates.otter.common.domain.audio.decorators

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.*
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

class OratureAudioFile : AudioFile {

    val logger = LoggerFactory.getLogger(OratureAudioFile::class.java)

    private val cues: List<AudioCue> = mutableListOf()
    private val extraCues: List<AudioCue> = mutableListOf()
    private val cueParser = OratureCueParser(this)

    private val cueMap: MutableMap<OratureCueType, MutableList<AudioMarker>> = mutableMapOf(
        OratureCueType.VERSE to mutableListOf()
    )

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
        cueMap.putAll(cueParser.parse())
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
            val verseEnd = if (match.groupCount() == 3) match.group(2).toInt() else verseStart
            val marker = VerseMarker(verseStart, verseEnd, location)
            cueMap[OratureCueType.VERSE]!!.add(marker)
        }
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
                match!!.groupValues.get(1)!!
            } else {
                match!!.groupValues.first()!!
            }
            AudioCue(it.location, "orature-vm-${label}")
        }
        cues.addAll(mapped)
    }

    private fun getCuesFromMap(type: OratureCueType): List<AudioMarker> {
        return cueMap[type] ?: listOf()
    }

    private fun clearCuesFromMap(type: OratureCueType) {
        cueMap[type]?.clear()
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

class VerseMarker(val start: Int, val end: Int, override val location: Int) : AudioMarker {
    override val label: String
        get() = if (end != start) "$start-$end" else "$start"

    override fun formatMarkerText() = "orature-vm-${label}"
}

enum class OratureCueType {
    CHUNK,
    VERSE,
    CHAPTER_TITLE,
    BOOK_TITLE,
    LICENSE
}

class OratureCueParser(val audio: OratureAudioFile) {


    private val verseMatcher = Pattern.compile("^orature-vm-(\\d+)(?:-(\\d+))?\$")
    private val chunkMatcher = Pattern.compile("^orature-chunk-(\\d+)")

    fun parse(): MutableMap<OratureCueType, MutableList<AudioMarker>> {
        val cueMap: MutableMap<OratureCueType, MutableList<AudioMarker>> = mutableMapOf(
            OratureCueType.VERSE to mutableListOf()
        )

        audio.getCues().forEach { cue ->
            val matchedVerses = matchMarkers(cue, verseMatcher, OratureCueType.VERSE, cueMap)
            if (!matchedVerses) matchMarkers(cue, chunkMatcher, OratureCueType.CHUNK, cueMap)
        }

        return cueMap
    }

    private fun matchMarkers(
        cue: AudioCue,
        matchPattern: Pattern,
        type: OratureCueType,
        cueMap: MutableMap<OratureCueType, MutableList<AudioMarker>>
    ): Boolean {
        val start: Int
        val end: Int
        val matcher = matchPattern.matcher(cue.label)
        if (matcher.matches()) {
            start = matcher.group(1).toInt()
            end = if (matcher.groupCount() > 1 && !matcher.group(2).isNullOrBlank()) {
                matcher.group(2).toInt()
            } else start
            val marker = when (type) {
                OratureCueType.VERSE -> VerseMarker(start, end, cue.location)
                OratureCueType.CHUNK -> TODO()
                OratureCueType.CHAPTER_TITLE -> TODO()
                OratureCueType.BOOK_TITLE -> TODO()
                OratureCueType.LICENSE -> TODO()
            }
            cueMap[type]!!.add(marker)
            return true
        }
        return false
    }
}