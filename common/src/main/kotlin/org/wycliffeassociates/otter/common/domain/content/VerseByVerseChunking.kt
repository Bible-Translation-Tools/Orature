package org.wycliffeassociates.otter.common.domain.content

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
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

    private val logger = LoggerFactory.getLogger(VerseByVerseChunking::class.java)

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
        chunks: List<AudioCue>,
        draftNumber: Int
    ) {
        val chapAudio = sourceAudio.getChapter(chapterNumber, workbook.target)
        val verseMarkers = AudioFile(chapAudio!!.file).metadata.getCues()
        val chunkRanges = mapCuesToRanges(chunks)
        val chunksToAdd = mutableListOf<Content>()
        for ((idx, chunk) in chunkRanges.withIndex()) {
            val verses = findVerseRange(mapCuesToRanges(verseMarkers), chunk)
            val start = verses.first()
            val end = verses.last()
            val v = accessor.getChunkText(projectSlug, chapterNumber, start, end)
            val text = StringBuilder().apply { v.forEach { append("$it\n") } }.toString()
            chunksToAdd.add(Content(
                idx + 1,
                "chunk",
                verses.first(),
                verses.last(),
                null,
                text,
                "usfm",
                ContentType.TEXT,
                draftNumber
            ))
        }
        chunkCreator(chunksToAdd)
        writeChunkFile(projectSlug, chapterNumber, chunksToAdd)
    }

    private fun writeChunkFile(projectSlug: String, chapterNumber: Int, chunksToAdd: List<Content>) {
        val factory = JsonFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        val mapper = ObjectMapper(factory)
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val chunks = mutableMapOf<Int, List<Content>>()

        val file: File = accessor.getChunkFile()
        try {
            if (file.exists() && file.length() > 0) {
                val typeRef: TypeReference<HashMap<Int, List<Content>>> =
                    object : TypeReference<HashMap<Int, List<Content>>>() {}
                val map: Map<Int, List<Content>> = mapper.readValue(file, typeRef)
                chunks.putAll(map)
                logger.error("restoring chunks")
            }
        } catch (e: MismatchedInputException) {
            // clear file if it can't be read
            file.writer().use {  }
        }

        logger.error("adding chunks to chapter: $chapterNumber")
        chunks[chapterNumber] = chunksToAdd

        logger.error("File with chunks is ${file.absolutePath}")

        file.writer().use {
            mapper.writeValue(it, chunks)
        }
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
                verses.add(verse.sort)
            }
            // verse inside chunk
            else if (verse.startLoc >= chunk.startLoc && verse.endLoc <= chunk.endLoc) {
                verses.add(verse.sort)
            }
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

    private data class VerseRange(val sort: Int, val startLoc: Int, val endLoc: Int)

    fun chunkVerseByVerse(
        projectSlug: String,
        draftNumber: Int
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
                ContentType.TEXT,
                draftNumber
            )
            chunksToAdd.add(content)
        }
        chunkCreator(chunksToAdd)
    }
}
