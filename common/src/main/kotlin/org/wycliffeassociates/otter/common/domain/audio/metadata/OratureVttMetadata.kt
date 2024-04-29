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
import org.wycliffeassociates.otter.common.data.audio.MarkerType
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.MarkerParser
import org.wycliffeassociates.otter.common.domain.audio.OratureCueParser
import org.wycliffeassociates.otter.common.domain.audio.OratureMarkers
import java.io.File

private const val BIBLICAL_REFERENCES_CLASS_NAME = "c.u23003"

class OratureVttMetadata(val vttFile: File, val markers: OratureMarkers = OratureMarkers()) : CueMetadata {

    private val _cues = mutableListOf<AudioMarker>()

    fun parseVTTFile() {
        val vtt = VTTParser().parseDocument(vttFile)
        val references = vtt.getCueContentsOfTag(BIBLICAL_REFERENCES_CLASS_NAME)
        val cues = mutableListOf<AudioCue>()
        for (marker in references) {
            val startMs = (marker.startTimeUs / 1000).toInt()
            val startFrame = (startMs * DEFAULT_SAMPLE_RATE / 1000)

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
                }
            }
        }
    }

    fun write() {
        vttFile.delete()
        val cues = markers.getMarkers()

        val bookSlug: String? = cues.find { it is BookMarker }
            ?.let {
                it as BookMarker
                it.bookSlug
            }

        val chapterNumber: Int? = cues.find { it is ChapterMarker }
            ?.let {
                it as ChapterMarker
                it.chapterNumber
            }

        if (cues.isEmpty()) {
            return
        }
        vttFile.createNewFile()
        val vttCues = cues
            .sortedBy { it.location }
            .map {
                WebVttCue(
                    WebvttCueInfo(
                        Cue.Builder()
                            .addMarkup(
                                "<c.u23003>" +
                                        "${OratureMarkerConverter.toBiblicalReference(it, bookSlug, chapterNumber)}" +
                                        "</c.u23003>"
                            )
                            .build(),
                        (((it.location * 1000L) / DEFAULT_SAMPLE_RATE.toFloat()) * 1000).toLong(),
                        (((it.location * 1000L) / DEFAULT_SAMPLE_RATE.toFloat()) * 1000).toLong()
                    )
                )
            }

        WebVttDocumentWriter(vttFile).writeDocument(vttCues)
    }

    override fun addCue(location: Int, label: String) {
    }

    override fun getCues(): List<AudioCue> {
        return _cues.map { it.toCue() }
    }

    override fun clearMarkers() {}
}