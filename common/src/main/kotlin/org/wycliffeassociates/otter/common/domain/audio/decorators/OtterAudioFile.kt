package org.wycliffeassociates.otter.common.domain.audio.decorators

import org.wycliffeassociates.otter.common.audio.*
import java.io.File

class OratureAudioFile : AudioFile {

    private val cues: List<AudioCue> = mutableListOf()
    private val extraCues: List<AudioCue> = mutableListOf()

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
    }

    constructor(): super() {
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

    override fun addCue(location: Int, label: String) {
        super.addCue(location, label)
        cues as MutableList
        cues.add(AudioCue(location, label))
        metadata.addCue(location, label)
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
}