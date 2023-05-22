package org.wycliffeassociates.otter.common.domain.audio

import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata

class SourceAudioFile : AudioFile {
    constructor(
        file: File,
    ) : super(file, WavMetadata(listOf(CueChunk()))) {
        cues.addAll(this.getCues())
        separateOratureCues(cues)
    }

    private val extraCues: List<AudioCue> = mutableListOf()

    private val cues = mutableListOf<AudioCue>()
    private val verses = mutableListOf<AudioCue>()
    private val chunks = mutableListOf<AudioCue>()

    fun getVerses(): List<AudioCue> {
        return verses
    }

    fun getChunks(): List<AudioCue> {
        return chunks
    }

    override fun update() {
        clearCues()
        verses.forEach { addCue(it.location, "orature-vm-${it.label}") }
        chunks.forEach { addCue(it.location, "orature-chunk-${it.label}") }
        extraCues.forEach { addCue(it.location, it.label) }
        super.update()
    }

    fun addVerse(cue: AudioCue) {
        addVerses(listOf(cue))
    }

    fun addVerses(cues: List<AudioCue>) {
        verses as MutableList
        verses.addAll(cues)
    }

    fun addChunk(cue: AudioCue) {
        addChunks(listOf(cue))
    }

    fun addChunks(cues: List<AudioCue>) {
        chunks as MutableList
        chunks.addAll(cues)
    }

    fun clearChunks() {
        chunks as MutableList
        chunks.clear()
    }

    private fun separateOratureCues(allCues: List<AudioCue>) {
        val chunkMarkerRegex = Regex("^orature-chunk-(\\d+)$")
        val verseMarkerRegex = Regex("^orature-vm-(\\d+)$")
        val loneDigitRegex = Regex("^\\d+$")
        val numberRegex = Regex("(\\d+)")

        // Pull out Orature namespaced cues (verses and chunks)
        val verseCues = allCues.filter { it.label.matches(verseMarkerRegex) }
        val chunkCues = allCues.filter { it.label.matches(chunkMarkerRegex) }

        // for the remaining cues, look for markers which could be intended as verse markers
        val leftoverCues = allCues.filter { !verseCues.contains(it) && !chunkCues.contains(it) }
        val loneDigits = leftoverCues.filter { it.label.trim().matches(loneDigitRegex) }

        // potential cues have text or whitespace with a number (could be something like "Verse 1")
        val potentialCues = leftoverCues
            .filter { !loneDigits.contains(it) }
            .filter { numberRegex.containsMatchIn(it.label) }
            .map {
                val match = numberRegex.find(it.label)
                val label = match!!.groupValues.first()!!
                AudioCue(it.location, label)
            }

        if (verseCues.isNotEmpty() || chunkCues.isNotEmpty()) {
            addMatchingCues(verses, verseCues, verseMarkerRegex)
            addMatchingCues(chunks, chunkCues, chunkMarkerRegex)
        } else if (loneDigits.isNotEmpty()) {
            addMatchingCues(verses, loneDigits.map { AudioCue(it.location, it.label.trim()) }, loneDigitRegex)
        } else if (potentialCues.isNotEmpty()) {
            addMatchingCues(verses, potentialCues, numberRegex)
        }
        extraCues as MutableList
        extraCues.addAll(leftoverCues)
    }

    private fun addMatchingCues(cueListToAddTo: List<AudioCue>, baseCueList: List<AudioCue>, regex: Regex) {
        cueListToAddTo as MutableList
        val mapped = baseCueList.map {
            val match = regex.find(it.label)
            val groups = match!!.groupValues
            val label = if (groups.size > 1) {
                match!!.groupValues.get(1)!!
            } else {
                match!!.groupValues.first()!!
            }
            AudioCue(it.location, label)
        }
        cueListToAddTo.addAll(mapped)
    }
}
