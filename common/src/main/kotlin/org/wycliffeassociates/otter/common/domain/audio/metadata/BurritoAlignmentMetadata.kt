package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.bibletranslationtools.kotlinscripturealignment.BurritoAudioAlignment
import org.bibletranslationtools.vtt.Cue
import org.bibletranslationtools.vtt.WebVttCue
import org.bibletranslationtools.vtt.WebVttDocument
import org.bibletranslationtools.vtt.WebvttCueInfo
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.CueMetadata
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.UnknownMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureCueParser
import java.io.File

class BurritoAlignmentMetadata(
    private val burritoTimingFile: File,
) : CueMetadata {

    private val _cues = mutableListOf<AudioMarker>()
    private val markers = OratureMarkers()
    fun parseTimings(): OratureMarkers {
        val timings = BurritoAudioAlignment.load(burritoTimingFile)

        val references = timings.getVttCues()
        val cues = mutableListOf<AudioCue>()
        for (marker in references) {
            val startMs = (marker.startTimeUs / 1000.0)
            val startFrame = ((startMs * DEFAULT_SAMPLE_RATE) / 1000L).toInt()

            val oratureLabel = BiblicalReferencesParser.parseBiblicalReference(marker.content)
            if (oratureLabel != null) {
                cues.add(AudioCue(startFrame, oratureLabel))
            }
        }
        OratureCueParser.parse(cues).getMarkers().let { parsedMarkers ->
            parsedMarkers.forEach { marker ->
                when (marker) {
                    is BookMarker -> this.markers.addMarker(OratureCueType.BOOK_TITLE, marker)
                    is ChapterMarker -> this.markers.addMarker(OratureCueType.CHAPTER_TITLE, marker)
                    is VerseMarker -> this.markers.addMarker(OratureCueType.VERSE, marker)
                    is ChunkMarker -> this.markers.addMarker(OratureCueType.CHUNK, marker)
                    is UnknownMarker -> this.markers.addMarker(OratureCueType.UNKNOWN, marker)
                }
            }
        }

        return markers
    }

    fun write(audioLengthInFrames: Int) {
        val markers = markers.getMarkers()
        val bookSlug: String = markers.find { it is BookMarker }
            ?.let {
                it as BookMarker
                it.bookSlug
            }!!

        val chapterNumber: Int = markers.find { it is ChapterMarker }
            ?.let {
                it as ChapterMarker
                it.chapterNumber
            }!!

        write(markers, bookSlug, chapterNumber, audioLengthInFrames)
    }

    fun write(
        markers: List<AudioMarker>,
        bookSlug: String,
        chapterNumber: Int,
        audioLengthInFrames: Int
    ) {
        if (markers.isEmpty()) {
            return
        }

        val alignment = BurritoAudioAlignment.load(burritoTimingFile)

        val vttCues = markers
            .sortedBy { it.location }
            .map {
                val tag = OratureMarkerConverter.toBiblicalReference(it, bookSlug, chapterNumber)!!
                WebVttDocument.WebVttCueContent(
                    tag,
                    tag,
                    WebVttCue(
                        WebvttCueInfo(
                            Cue.Builder()
                                .addMarkup("<c.u23003>$tag</c.u23003>")
                                .build(),
                            framesToUs(it.location),
                            framesToUs(it.location)
                        )
                    )
                )
            }

        assignEndTimes(vttCues, audioLengthInFrames)

        alignment.setRecordsFromVttCueContent(vttCues)
        alignment.update()
    }

    private fun assignEndTimes(cues: List<WebVttDocument.WebVttCueContent>, audioLengthInFrames: Int) {
        cues.forEachIndexed { i, content ->
            val cue = content.cue
            if (i == cues.lastIndex) {
                cue.endTimeUs = framesToUs(audioLengthInFrames)
            } else {
                cues[i].cue.endTimeUs = cues[i + 1].cue.startTimeUs
            }
        }
    }

    override fun addCue(location: Int, label: String) {}

    override fun getCues(): List<AudioCue> {
        return _cues.map { it.toCue() }
    }

    override fun clearMarkers() {}
}

internal fun framesToUs(frames: Int): Long {
    val us = (frames.toLong() * (1000000 / DEFAULT_SAMPLE_RATE.toDouble())).toLong()
    return us
}
