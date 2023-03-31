package org.wycliffeassociates.otter.common.domain.content

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.MaybeSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.Chunkification
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.SourceAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.VersificationTreeBuilder
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class CreateChunks(
    private val projectFilesAccessor: ProjectFilesAccessor,
    private val sourceAudioAccessor: SourceAudioAccessor,
    private val chunkCreator: (List<Content>) -> Unit,
    private val chapterNumber: Int,
    private val workbook: Workbook
) {
    private val logger = LoggerFactory.getLogger(CreateChunks::class.java)

    fun createUserDefinedChunks(
        projectSlug: String,
        chunks: List<AudioCue>,
        draftNumber: Int
    ) {
        logger.info("Creating ${chunks.size} user defined chunks for project: $projectSlug chapter: $chapterNumber")
        val chapAudio = sourceAudioAccessor.getChapter(chapterNumber, workbook.target)
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

    /**
     * Creates chunks from the verses in the text of the project.
     *
     * @param versificationRepository the versification repository to load the appropriate versification to preallocate
     * @param projectSlug the slug of the project to create chunks for
     * @param draftNumber the draft number to create chunks for
     */
    fun createChunksFromVerses(
        versificationRepository: IVersificationRepository,
        projectSlug: String,
        draftNumber: Int
    ) {
        ResourceContainer
            .load(workbook.source.resourceMetadata.path).use { rc ->
                val vrsSlug = rc.manifest.projects.firstOrNull { !it.versification.isNullOrEmpty() }?.versification ?: ""
                versificationRepository.getVersification(vrsSlug)
            }.map {
                val allocatedVerses = preallocateVerses(it, workbook.target, chapterNumber, draftNumber)
                val versesFromText = getVersesFromText(projectSlug, chapterNumber, draftNumber)
                val finalizedVerses = overlayVerses(allocatedVerses, versesFromText)
                chunkCreator(finalizedVerses)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /**
     * Fill in the text of the versification allocated content using the content retrieved from the text.
     *
     * @return a list of content objects based on the versification, with the text field filled in
     */
    private fun overlayVerses(allocatedVerses: List<Content>, versesFromText: List<Content>): List<Content> {
        val verses = mutableListOf<Content>()
        allocatedVerses.forEach {
            val found = versesFromText.find { v -> v.start == it.start }
            if (found != null) {
                val overwritten = it.copy(
                    text = found.text,
                    start = found.start,
                    end = found.end,
                    bridged = found.bridged
                )
                verses.add(overwritten)
            } else {
                verses.add(it)
            }
        }
        markBridgedVerses(verses)
        return verses
    }

    /**
     * Scans through the list of verses and marks any verses that are bridged.
     *
     * It does this by finding the first instance of the start and end matching and marks all future verses
     * through the start and end matching again (which would signify the last verse in the bridge).
     *
     * @param verses the list of verses to mark, this updates the list in place
     */
    private fun markBridgedVerses(verses: MutableList<Content>) {
        verses.sortBy { it.sort }
        var bridge = false
        for (i in 0 until verses.size) {
            if (bridge) {
                verses[i].bridged = true
            }
            if (verses[i].start == verses[i].end) {
                bridge = false
            }
            if (verses[i].start != verses[i].end) {
                bridge = true
            }
        }
    }


    /**
     * Creates a list of content using the versification of the project. As it is being allocated from versification,
     * the text entry will be empty.
     *
     * @param versification the versification to use for preallocation
     * @param book the book to allocate
     * @param chapterNumber the chapter to allocate
     * @param draftNumber the number for the draft being created
     *
     * @return a list of content objects, one for each verse in the chapter
     */
    private fun preallocateVerses(
        versification: Versification,
        book: Book,
        chapterNumber: Int,
        draftNumber: Int
    ): List<Content> {
        val chunks = mutableListOf<Content>()
        val verseCount = versification.getVersesInChapter(book.slug, chapterNumber)
        for (verseNumber in 1..verseCount) {
            chunks.add(
                Content(
                    verseNumber,
                    "verse",
                    verseNumber,
                    verseNumber,
                    null,
                    "",
                    "usfm",
                    ContentType.TEXT,
                    draftNumber
                )
            )
        }
        return chunks
    }

    /**
     * Gets all content from parsing text content of the resource container
     *
     * @param projectSlug the project to get content from
     * @param chapterNumber the chapter to get content from
     * @param draftNumber the number for the draft being created
     * @return a list of content objects from the text content
     */
    private fun getVersesFromText(projectSlug: String, chapterNumber: Int, draftNumber: Int): List<Content> {
        logger.info("Creating chunks from project $projectSlug from verses, draft $draftNumber")
        val verses = mutableListOf<Content>()
        projectFilesAccessor.getChapterContent(
            projectSlug,
            chapterNumber,
            showVerseNumber = false
        ).forEachIndexed { idx, content ->
            content.sort = idx + 1
            content.draftNumber = draftNumber
            verses.add(content)
        }
        return verses
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
