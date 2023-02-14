package org.wycliffeassociates.otter.common.domain.content

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.Chunkification
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.domain.audio.SourceAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class CreateChunks(
    private val projectFilesAccessor: ProjectFilesAccessor,
    private val sourceAudioAccessor: SourceAudioAccessor,
    private val chunkCreator: (List<Content>) -> Unit,
    private val chapterNumber: Int,
    private val targetBook: Book
) {
    private val logger = LoggerFactory.getLogger(CreateChunks::class.java)

    fun createUserDefinedChunks(
        projectSlug: String,
        chunks: List<AudioCue>,
        draftNumber: Int
    ) {
        logger.info("Creating ${chunks.size} user defined chunks for project: $projectSlug chapter: $chapterNumber")
        val chapAudio = sourceAudioAccessor.getChapter(chapterNumber, targetBook)
        val sa = SourceAudioFile(chapAudio!!.file)
        val verseMarkers = sa.getVerses()
        val chunkRanges = mapCuesToRanges(chunks)
        val verseRanges = mapCuesToRanges(verseMarkers)
        val chunksToAdd = mutableListOf<Content>()

        for ((idx, chunk) in chunkRanges.withIndex()) {
            val verses = findVerseRange(verseRanges, chunk)

            // use the chapter range and text if there are no verse markers (which would make the verse range empty)
            val chapterText = projectFilesAccessor.getChapterText(projectSlug, chapterNumber)
            var start = 1
            var end = chapterText.size
            var text = ""

            // adjust verse range and text based on verse markers
            if (verses.isNotEmpty()) {
                start = verses.first()
                end = verses.last()
                val v = projectFilesAccessor.getChunkText(projectSlug, chapterNumber, start, end)
                text = StringBuilder().apply { v.forEach { append("$it\n") } }.toString()
            } else {
                text = StringBuilder().apply { chapterText.forEach { append(it) } }.toString()
            }
            chunksToAdd.add(
                Content(
                    idx + 1,
                    "chunk",
                    start,
                    end,
                    null,
                    text,
                    "usfm",
                    ContentType.TEXT,
                    draftNumber
                )
            )
        }
        chunkCreator(chunksToAdd)
        writeChunkFile(projectSlug, chapterNumber, chunksToAdd)
    }

    fun createChunksFromVerses(
        projectSlug: String,
        draftNumber: Int
    ) {
        val chunksToAdd = mutableListOf<Content>()
        projectFilesAccessor.getChapterContent(projectSlug, chapterNumber).forEachIndexed { idx, content ->
            content.sort = idx + 1
            content.draftNumber = draftNumber
            chunksToAdd.add(content)
        }
        chunkCreator(chunksToAdd)
    }

    private fun writeChunkFile(projectSlug: String, chapterNumber: Int, chunksToAdd: List<Content>) {
        val factory = JsonFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        val mapper = ObjectMapper(factory)
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val chunks = mutableMapOf<Int, List<Content>>()

        val file: File = projectFilesAccessor.getChunkFile().apply {
            // Create empty file if it doesn't exist
            parentFile.mkdirs()
            createNewFile()
        }
        try {
            if (file.exists() && file.length() > 0) {
                val map: Chunkification = mapper.readValue(file)
                chunks.putAll(map)
                logger.error("restoring chunks")
            }
        } catch (e: MismatchedInputException) {
            // clear file if it can't be read
            file.writer().use { }
        }

        logger.error("adding chunks to chapter: $chapterNumber")
        chunks[chapterNumber] = chunksToAdd

        logger.error("File with chunks is ${file.absolutePath}")

        file.writer().use {
            mapper.writeValue(it, chunks)
        }
    }

    private data class VerseRange(val sort: Int, val startLoc: Int, val endLoc: Int)

    private fun findVerseRange(verseMarkers: List<VerseRange>, chunk: VerseRange): List<Int> {
        val verses = mutableListOf<Int>()
        if (verseMarkers.isEmpty()) {
            logger.error("Cannot find verse range, verse markers list is empty")
            return verses
        }

        for (verse in verseMarkers) {
            val chunkStartsAfterVerseStart = chunk.startLoc >= verse.startLoc
            val chunkStartsBeforeVerseEnd = chunk.startLoc <= verse.endLoc

            val chunkEndsAfterVerseStart = chunk.endLoc >= verse.startLoc
            val chunkEndsBeforeVerseEnd = chunk.endLoc <= verse.endLoc

            val verseStartsAfterChunkStart = verse.startLoc >= chunk.startLoc
            val verseEndsBeforeChunkEnd = verse.endLoc <= chunk.endLoc

            // chunk start inside verse
            if (chunkStartsAfterVerseStart && chunkStartsBeforeVerseEnd) {
                verses.add(verse.sort)
            }
            // chunk end inside verse
            else if (chunkEndsAfterVerseStart && chunkEndsBeforeVerseEnd) {
                verses.add(verse.sort)
            }
            // verse inside chunk
            else if (verseStartsAfterChunkStart && verseEndsBeforeChunkEnd) {
                verses.add(verse.sort)
            }
        }

        // If nothing is found, given that the last verse should reach end of file, or Int.MAX, it is likely that
        // the first verse has been moved forward, and that the chunk exists before any verse markers.
        if (verses.isEmpty()) {
            verses.add(verseMarkers.first().sort)
        }
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
}
