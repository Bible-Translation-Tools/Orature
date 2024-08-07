package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.bibletranslationtools.vtt.Cue
import org.bibletranslationtools.vtt.VTTParser
import org.bibletranslationtools.vtt.WebVttCue
import org.bibletranslationtools.vtt.WebVttDocumentWriter
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

private const val BIBLICAL_REFERENCES_CLASS_NAME = "c.u23003"

class OratureVttMetadata(
    private val vttFile: File,
) : CueMetadata {

    private val _cues = mutableListOf<AudioMarker>()
    private val markers = OratureMarkers()
    fun parseVTTFile(): OratureMarkers {
        val vtt = VTTParser().parseDocument(vttFile)
        val references = vtt.getCueContentsOfTag(BIBLICAL_REFERENCES_CLASS_NAME)
        val cues = mutableListOf<AudioCue>()
        for (marker in references) {
            val startMs = (marker.startTimeUs / 1000L)
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

        vttFile.delete()
        vttFile.createNewFile()

        val vttCues = markers
            .sortedBy { it.location }
            .map {
                WebVttCue(
                    WebvttCueInfo(
                        Cue.Builder()
                            .addMarkup(
                                "<c.u23003>" +
                                        "${
                                            OratureMarkerConverter.toBiblicalReference(
                                                it,
                                                bookSlug,
                                                chapterNumber
                                            )
                                        }" +
                                        "</c.u23003>"
                            )
                            .build(),
                        framesToUs(it.location),
                        framesToUs(it.location)
                    )
                )
            }

        assignEndTimes(vttCues, audioLengthInFrames)

        WebVttDocumentWriter(vttFile).writeDocument(vttCues)
    }

    private fun assignEndTimes(cues: List<WebVttCue>, audioLengthInFrames: Int) {
        cues.forEachIndexed { i, cue ->
            if (i == cues.lastIndex) {
                cue.endTimeUs = framesToUs(audioLengthInFrames)
            } else {
                cues[i].endTimeUs = cues[i + 1].startTimeUs
            }
        }
    }

    private fun framesToUs(frames: Int): Long {
        return (((frames * 1000L) / DEFAULT_SAMPLE_RATE.toLong()) * 1000L)
    }

    override fun addCue(location: Int, label: String) {}

    override fun getCues(): List<AudioCue> {
        return _cues.map { it.toCue() }
    }

    override fun clearMarkers() {}
}