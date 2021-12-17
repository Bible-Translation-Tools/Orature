package org.wycliffeassociates.otter.common.audio.wav

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.wycliffeassociates.otter.common.audio.AudioCue

class VerseMarkerChunk : CueChunk() {
    private val extraCues: List<AudioCue> = mutableListOf()

    override fun addParsedCues(cueListBuilder: CueListBuilder) {
        val allCues = cueListBuilder.build()
        separateOratureCues(allCues)
    }

    override fun toByteArray(): ByteArray {
        if (cues.isEmpty()) {
            return ByteArray(0)
        }

        val outputCues = mutableListOf<AudioCue>()
        outputCues.addAll(cues.map { AudioCue(it.location, "orature-vm-${it.label}") })
        outputCues.addAll(extraCues)
        outputCues.sortBy { it.location }

        val cueChunkBuffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE + getCueChunkSize(outputCues))
        cueChunkBuffer.order(ByteOrder.LITTLE_ENDIAN)
        cueChunkBuffer.put(CUE_LABEL.toByteArray(Charsets.US_ASCII))
        cueChunkBuffer.putInt(CUE_DATA_SIZE * outputCues.size + CUE_COUNT_SIZE)
        cueChunkBuffer.putInt(outputCues.size)
        for (i in outputCues.indices) {
            cueChunkBuffer.put(createCueData(i, outputCues[i]))
        }
        val labelChunkArray = createLabelChunk(outputCues)
        val combinedBuffer = ByteBuffer.allocate(cueChunkBuffer.capacity() + labelChunkArray.size)
        combinedBuffer.put(cueChunkBuffer.array())
        combinedBuffer.put(labelChunkArray)
        return combinedBuffer.array()
    }

    private fun getCueChunkSize(cues: List<AudioCue>): Int {
        return CUE_HEADER_SIZE + (CUE_DATA_SIZE * cues.size)
    }

    private fun separateOratureCues(allCues: List<AudioCue>) {
        val oratureRegex = Regex("^orature-vm-(\\d+)$")
        val loneDigitRegex = Regex("^\\d+$")
        val numberRegex = Regex("(\\d+)")

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

    fun addMatchingCues(baseCueList: List<AudioCue>, regex: Regex) {
        cues as MutableList
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
        cues.addAll(mapped)
    }
}
