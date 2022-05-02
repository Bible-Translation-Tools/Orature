package org.wycliffeassociates.otter.common.domain.content

import java.text.Format
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

class VerseByVerseChunking(
    val directoryProvider: IDirectoryProvider,
    val workbook: Workbook,
    val chunkCreator: (List<Content>) -> Unit,
    val chapterNumber: Int
) {

    val accessor: ProjectFilesAccessor
    val sourceAudio: SourceAudioAccessor

    init {
        accessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            workbook.target.resourceMetadata,
            workbook.source
        )
        sourceAudio = SourceAudioAccessor(directoryProvider, workbook.source.resourceMetadata, workbook.source.slug)
    }

    fun chunkChunkByChunk(
        projectSlug: String,
        chunks: List<AudioCue>
    ) {
        val chapAudio = sourceAudio.getChapter(chapterNumber, workbook.target.resourceMetadata)
        val verseMarkers = AudioFile(chapAudio!!.file).metadata.getCues()
        val chunkRanges = mapCuesToRanges(chunks)
        val chunksToAdd = mutableListOf<Content>()
        for ((idx, chunk) in chunkRanges.withIndex()) {
            val verses = findVerseRange(mapCuesToRanges(verseMarkers), chunk)
            val start = verses.first()
            val end = verses.last()
            val v = accessor.getChunkText(projectSlug, chapterNumber, start, end)
            val text = StringBuilder().apply { v.forEach { append("$it\n") } }.toString()
            chunksToAdd.add(Content(idx + 1, "chunk", verses.first(), verses.last(), null, text, "usfm", ContentType.TEXT))
        }
        chunkCreator(chunksToAdd)
    }

    private fun findVerseRange(verseMarkers: List<VerseRange>, chunk: VerseRange): List<Int> {
        val verses = mutableListOf<Int>()

        for (verse in verseMarkers) {
            // chunk start inside verse
            if (chunk.startLoc >= verse.startLoc && chunk.startLoc <= verse.endLoc) {
                verses.add(verse.sort)
            }
            // chunk end inside verse
            else if (chunk.endLoc >= verse.startLoc && chunk.endLoc <= verse.endLoc) {
                println("matched second")
                verses.add(verse.sort)
            }
            // verse inside chunk
            else if (verse.startLoc >= chunk.startLoc && verse.endLoc <= chunk.endLoc) {
                println("matched third")
                verses.add(verse.sort)
            }
        }
        println("matching verses for chunk ${chunk.sort} is $verses")
        return verses
    }


    private fun mapCuesToRanges(cues: List<AudioCue>): List<VerseRange> {
        cues.sortedBy { it.location }
        val ranges = mutableListOf<VerseRange>()
        cues.forEachIndexed { idx, cue ->
            val end = if (cues.size > idx + 1) cues[idx + 1].location else Int.MAX_VALUE
            ranges.add(VerseRange(idx + 1, cue.location, end))
        }
        return ranges
    }

    private data class VerseRange(val sort: Int, val startLoc: Int, val endLoc: Int)

    fun chunkVerseByVerse(
        projectSlug: String,
    ) {
        val chunksToAdd = mutableListOf<Content>()
        accessor.getChapterText(projectSlug, chapterNumber).forEachIndexed { idx, str ->
            val verseNumber = idx + 1
            val content = Content(
                verseNumber,
                "verse",
                verseNumber,
                verseNumber,
                null,
                str,
                "usfm",
                ContentType.TEXT
            )
            chunksToAdd.add(content)
        }
        chunkCreator(chunksToAdd)
    }
}
