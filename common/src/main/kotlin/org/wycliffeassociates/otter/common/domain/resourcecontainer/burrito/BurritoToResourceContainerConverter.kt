package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.Role
import org.bibletranslationtools.scriptureburrito.container.BurritoContainer
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.TrackConfiguration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioMetadataFileFormat
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.audio.metadata.BurritoAlignmentMetadata
import org.wycliffeassociates.otter.common.domain.content.BibleFileNamer
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.IResourceContainerAccessor
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.Language
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.MediaProject
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.collections.HashMap

internal typealias IngredientsByBook = Map<String, List<Pair<String, IngredientSchema>>>
internal typealias FilesByBook = Map<String, FilesByChapter>
internal typealias FilesByChapter = Map<Int, List<File>>

internal val books = arrayOf(
    "gen", "exo", "lev", "num", "deu", "jos", "jdg", "rut", "1sa", "2sa", "1ki", "2ki", "1ch", "2ch",
    "ezr", "neh", "est", "job", "psa", "pro", "ecc", "sng", "isa", "jer", "lam", "ezk", "dan", "hos",
    "jol", "amo", "oba", "jon", "mic", "nam", "hab", "zep", "hag", "zec", "mal", "mat", "mrk", "luk",
    "jhn", "act", "rom", "1co", "2co", "gal", "eph", "php", "col", "1th", "2th", "1ti", "2ti", "tit",
    "phm", "heb", "jas", "1pe", "2pe", "1jn", "2jn", "3jn", "jud", "rev"
)

private val SUPPORTED_AUDIO_MIME = setOf("audio/wav", "audio/mpeg")
private val SUPPORTED_AUDIO_FILES = setOf("mp3", "wav")

internal val ot = books.slice(0 until 40)
internal val nt = books.slice(40 until 66)

private fun mapBookNumberToUfwBookNumber(bookIndex: Int): Int = if (bookIndex <= ot.size) bookIndex + 1 else bookIndex + 2

internal fun getBookNumber(bookSlug: String): Int {
    return mapBookNumberToUfwBookNumber(books.indexOf(bookSlug))
}

internal fun getTestament(bookSlug: String): String {
    return when (bookSlug) {
        in ot -> "bible-ot"
        in nt -> "bible-nt"
        else -> ""
    }
}

private val usfmFilenamePattern = "./{booknum}-{book}.usfm"
private val filenamePattern = "{language}_{title}_{book}_c{chapter}.{extension}"
private val DEFAULT_TITLE_CODE = "reg"

open class BurritoToResourceContainerConverter @Inject constructor(
    val directoryProvider: IDirectoryProvider
) {

    private val logger = LoggerFactory.getLogger(BurritoToResourceContainerConverter::class.java)

    var tempDir = directoryProvider.tempDirectory

    fun convert(
        burrito: File,
        outputFile: File
    ): Boolean {

        tempDir = File(directoryProvider.tempDirectory, burrito.name).apply { mkdirs() }

        if (outputFile.extension == "zip") outputFile.outputStream()
            .use { ZipOutputStream(it).use { } }
        val burrito = BurritoContainer.load(burrito)
        burrito.use {
            val metadata = it.manifest
            ResourceContainer.create(outputFile) {
                val (projects, media) = processContentInBurrito(
                    metadata,
                    burrito.accessor,
                    this.accessor
                )
                this.manifest = Manifest(
                    dublinCore = dublinCoreFromBurrito(metadata),
                    projects = projects,
                    checking = Checking(),
                )
                this.media = media
                this.write()
            }
        }
        return true
    }

    internal fun processContentInBurrito(
        burrito: MetadataSchema,
        inputAccessor: IContainerAccessor,
        outputAccessor: IResourceContainerAccessor
    ): Pair<List<Project>, MediaManifest> {
        val ingredientsByBook = getIngredientsByBook(burrito)
        var usfmFilesByBook = getUSFMIngredients(ingredientsByBook)
        val chapterAudioByBook = createChapterAudioIngredients(
            burrito,
            ingredientsByBook,
            inputAccessor
        )

        val versification = getVersification(burrito, usfmFilesByBook, chapterAudioByBook)

        usfmFilesByBook = moveUSFMFiles(usfmFilesByBook, inputAccessor, outputAccessor)
        moveAudioFiles(burrito, chapterAudioByBook, outputAccessor)

        val mediaManifest = createMediaManifest(burrito, chapterAudioByBook)
        val projects = createProjects(
            burrito,
            versification,
            ingredientsByBook.keys.filter { it in usfmFilesByBook.keys },
            usfmFilenamePattern
        )

        return Pair(projects, mediaManifest)
    }

    /**
     * Given a book and list of ingredients, this organizes the ingredients into a map keyed
     * by chapter number. Ingredients that span multiple chapters will be listed under all chapters
     * in that span.
     */
    protected fun groupAudioIngredientsByChapter(
        book: String,
        ingredients: List<Pair<String, IngredientSchema>>
    ): Map<Int, MutableList<Pair<String, IngredientSchema>>> {
        val groupedByChapter = hashMapOf<Int, MutableList<Pair<String, IngredientSchema>>>()
        for (item in ingredients) {
            val (_, ingredient) = item
            val scope = ingredient.scope?.get(book.uppercase(Locale.US))!!
            for (ref in scope) {
                val chapters = parseChapterRangeFromBibleReferences(ref)
                for (chapter in chapters) {
                    groupedByChapter.putIfAbsent(chapter, mutableListOf())
                    groupedByChapter[chapter]!!.add(item)
                }
            }
        }
        return groupedByChapter
    }

    /**
     * Get all ingredients of a book whose scope is the entire book
     */
    protected fun getCompleteBookIngredients(
        book: String,
        ingredients: List<Pair<String, IngredientSchema>>
    ): List<Pair<String, IngredientSchema>> {
        return ingredients.filter { (_, ingredient) ->
            ingredient.scope?.get(book.uppercase())?.isEmpty() ?: false
        }
    }

    protected fun parseChapterRangeFromBibleReferences(reference: String): List<Int> {
        val regex =
            Regex("^([1-9][0-9]*)(?:-([1-9][0-9]*))?(?::([1-9][0-9]*))?(?:-([1-9][0-9]*))?$")
        val matchResult = regex.find(reference) ?: return emptyList()

        val (startChapter, endChapter, _, _) = matchResult.destructured

        return when {
            endChapter.isNotEmpty() -> {
                val start = startChapter.toInt()
                val end = endChapter.toInt()
                (start..end).toList()
            }

            startChapter.isNotEmpty() -> {
                listOf(startChapter.toInt())
            }

            else -> {
                emptyList()
            }
        }
    }

    /**
     * Copies the audio file out of the container into the working temp directory, as well as its
     * timing file if applicable.
     */
    protected fun handleSingleChapterAudioIngredient(
        audioFile: String,
        ingredients: List<Pair<String, IngredientSchema>>,
        inputAccessor: IContainerAccessor
    ): List<File> {
        val filesToCopy = mutableListOf<File>()
        val timing = findMatchingTimingFile(audioFile, ingredients, inputAccessor)
        timing?.let {
            convertBurritoTimingToOratureTiming(
                audioFile,
                timing.first,
                inputAccessor
            )?.let {
                filesToCopy.add(it)
            }
        }

        val name = File(audioFile).name
        val chapterAudio = File(tempDir, name)
        inputAccessor.getInputStream(audioFile).use { ifs ->
            chapterAudio.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }
        filesToCopy.add(chapterAudio)

        return filesToCopy
    }

    protected fun extractTempAudioAndTiming(
        audioFile: String,
        timingFile: String,
        inputAccessor: IContainerAccessor
    ): Pair<File, File> {
        val audioName = File(audioFile).name
        val timingName = File(timingFile).name
        val tempAudioFile = File(tempDir, audioName).apply { createNewFile() }
        val tempTimingFile = File(tempDir, timingName).apply { createNewFile() }
        inputAccessor.getInputStream(audioFile).use { ifs ->
            tempAudioFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }

        inputAccessor.getInputStream(timingFile).use { ifs ->
            tempTimingFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }

        return Pair(tempAudioFile, tempTimingFile)
    }

    protected fun getRelevantAudioSections(
        chapter: Int,
        audio: File,
        timing: File
    ): List<MarkerLocation> {
        val metadata = BurritoAlignmentMetadata(
            timing,
            audio,
            chapter
        ).parseTimings()

        val extraMetadata = BurritoAlignmentMetadata(
            timing,
            audio
        ).parseTimings()

        var markers = buildList {
            addAll(metadata.getMarkers(OratureCueType.BOOK_TITLE))
            addAll(metadata.getMarkers(OratureCueType.CHAPTER_TITLE))
            addAll(metadata.getMarkers(OratureCueType.VERSE))
        }.sortedBy { it.location }

        var extraMarkers = buildList {
            addAll(extraMetadata.getMarkers(OratureCueType.BOOK_TITLE))
            addAll(extraMetadata.getMarkers(OratureCueType.CHAPTER_TITLE))
            addAll(extraMetadata.getMarkers(OratureCueType.VERSE))
        }
            .filter { it !in markers }
            .sortedBy { it.location }

        val chapterMarkers = markers.filter {
            it is ChapterMarker
        }

        var filterBeforeChapter: Int = 0
        var filterAfterChapter: Int = Int.MAX_VALUE
        chapterMarkers?.let {
            val startChapter = chapterMarkers.find { it.sort == chapter }
            val startIndex = chapterMarkers.indexOf(startChapter)
            val nextChapter = chapterMarkers.getOrNull(startIndex + 1)

            startChapter?.location?.let { filterBeforeChapter = it }
            nextChapter?.location?.let { filterAfterChapter = it }

            if (nextChapter == null && extraMarkers.isNotEmpty() && markers.isNotEmpty()) {
                extraMarkers.firstOrNull { firstUnmatched -> firstUnmatched.location > markers.last().location }?.let {
                    logger.info("No next chapter marker, but should not read past: ${it.label}")
                    filterAfterChapter = it.location
                }
            }
        }

        markers = markers.filter { it.location in filterBeforeChapter until filterAfterChapter }

        val absoluteMarkerEnd = filterAfterChapter
        val relevantSections = mutableListOf<MarkerLocation>()
        for (i in markers.indices) {
            val start = markers[i].location
            val end = if (i == markers.size - 1) absoluteMarkerEnd else markers[i + 1].location
            relevantSections.add(Pair(markers[i], start..end))
        }
        return relevantSections
    }


    /**
     * Reads sections from multiple files to assemble a completed chapter audio wav file
     */
    protected fun constructChapterAudio(
        chapter: Int,
        fileNamer: BibleFileNamer,
        relevantSections: Map<File, List<MarkerLocation>>
    ): File {
        logger.info("Constructing chapter audio for chapter: ${chapter} from:")
        relevantSections.forEach { (name, _) ->
            logger.info(name.name)
        }
        val outputFile = File(tempDir, fileNamer.chapterFileName(chapter))
        val wav =
            WavFile(outputFile, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        val byteBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val listified = relevantSections
            .toList()
            .sortedBy { (file, markers) ->
                markers
                    .filter { it.first is VerseMarker }
                    .minOf { (it.first as VerseMarker).start }
            }

        val newMarkers = mutableListOf<AudioMarker>()
        var pos = 0
        for ((file, markers) in listified) {
            val audio = OratureAudioFile(file)
            for (marker in markers) {
                newMarkers.add(marker.first.clone(pos / wav.frameSizeInBytes))
                logger.info("Reading ${marker.second} for ${marker.first.label} from $file...")
                val (type, timing) = marker
                audio.reader(timing.first, timing.last).use {
                    it.open()
                    while (it.hasRemaining()) {
                        val read = it.getPcmBuffer(byteBuffer)
                        if (read < 0 ) {
                            println("ERROR: read is negative, ${file.name}, ${timing}")
                            println("Position is at: ${it.framePosition}")
                        }
                        wav.writer(true).use {
                            it.write(byteBuffer, 0, read)
                        }
                        pos += read
                    }
                }
                logger.info("Section copied: ${marker.first.label}!")
            }
        }
        OratureAudioFile(outputFile).apply {
            newMarkers.forEach { addMarker(it) }
            this.update()
        }
        return outputFile
    }

    protected fun handleConstructingChapterAudioIngredient(
        book: String,
        chapter: Int,
        ingredients: List<Pair<String, IngredientSchema>>,
        fileNamer: BibleFileNamer,
        inputAccessor: IContainerAccessor,
    ): List<File> {
        val relevantSections = hashMapOf<File, List<MarkerLocation>>()
        val audioIngredients = ingredients.filter { it.second.mimeType in SUPPORTED_AUDIO_MIME }
        for (item in audioIngredients) {
            val (audioFile, _) = item
            val (timingFile, _) = findMatchingTimingFile(audioFile, ingredients, inputAccessor) ?: continue
            val (tempAudio, tempTiming) = extractTempAudioAndTiming(
                audioFile,
                timingFile,
                inputAccessor
            )
            val audioSections = getRelevantAudioSections(chapter, tempAudio, tempTiming)
            relevantSections[tempAudio] = audioSections
        }
        return listOf(
            constructChapterAudio(
                chapter,
                fileNamer,
                relevantSections
            )
        )
    }

    internal fun createChapterAudioIngredients(
        burrito: MetadataSchema,
        ingredientsByBook: IngredientsByBook,
        inputAccessor: IContainerAccessor
    ): FilesByBook {
        val defaultLocale = burrito.meta.defaultLocale
        val resourceAbbr = burrito.identification?.abbreviation?.get(defaultLocale) ?: "reg"
        val filtered = filterAcceptedAudioFormats(burrito, ingredientsByBook)
        val reconstructed = hashMapOf<String, HashMap<Int, MutableList<File>>>()
        for ((book, ingredients) in filtered) {
            reconstructed.putIfAbsent(book, hashMapOf())
            val completeBooks = getCompleteBookIngredients(book, ingredients)
            val groupedByChapter = groupAudioIngredientsByChapter(book, ingredients)
            for ((chapter, ingredients) in groupedByChapter) {
                val fileNamer = BibleFileNamer(burrito.meta.defaultLocale, book, resourceAbbr)
                val audioFiles = ingredients.filter { (name, ing) -> File(name).extension in SUPPORTED_AUDIO_FILES }
                logger.info("Chapter $chapter has ${audioFiles.size} audio files comprising it:")
                audioFiles.forEach {
                    logger.info("${it.first}")
                }
                val files = when (audioFiles.size) {
                    1 -> handleSingleChapterAudioIngredient(
                        audioFiles[0].first,
                        ingredients,
                        inputAccessor
                    )

                    else -> handleConstructingChapterAudioIngredient(
                        book,
                        chapter,
                        ingredients,
                        fileNamer,
                        inputAccessor
                    )
                }
                reconstructed[book]!!.putIfAbsent(chapter, mutableListOf())
                reconstructed[book]!![chapter]!!.addAll(files)
            }
        }
        return reconstructed
    }

    internal fun convertBurritoTimingToOratureTiming(
        file: String,
        timing: String,
        inputAccessor: IContainerAccessor
    ): File? {
        if (File(file).extension !in SUPPORTED_AUDIO_FILES) return null

        val audioFile = File(tempDir, File(file).name)
        val timingFile = File(tempDir, File(timing).name)

        inputAccessor.getInputStream(file).use { ifs ->
            audioFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }

        inputAccessor.getInputStream(timing).use { ifs ->
            timingFile.outputStream().use { ofs ->
                ifs.transferTo(ofs)
            }
        }

        val audio = OratureAudioFile(audioFile)
        audio.clearMarkers()

        val markers = getMarkersFromBurritoTimining(timingFile, audioFile)

        for (marker in markers) {
            audio.addMarker(marker)
        }
        audio.update()

        File(tempDir, file).outputStream().use { output ->
            audioFile.inputStream().use { input ->
                input.transferTo(output)
            }
        }

        if (audio.file.extension == "mp3") { // For Mp3, copy a corresponding cue file out if it exists
            val cuePath = file.replace("mp3", "cue")
            val cueFile = File(File(cuePath).name)

            if (inputAccessor.fileExists(cuePath)) {
                File(tempDir, cuePath).outputStream().use { output ->
                    cueFile.inputStream().use { input ->
                        input.transferTo(output)
                    }
                }
                return cueFile
            }
        }
        return null
    }

    internal fun moveUSFMFiles(
        usfmFilesByBook: IngredientsByBook,
        inputAccessor: IContainerAccessor,
        outputAccessor: IResourceContainerAccessor
    ): IngredientsByBook {
        val newIngredientsByBook = hashMapOf<String, List<Pair<String, IngredientSchema>>>()
        for ((book, usfmFiles) in usfmFilesByBook) {
            if (usfmFiles.isEmpty()) continue
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = mapBookNumberToUfwBookNumber(bookIndex)
            val (usfmFile, ingredient) = usfmFiles.first()
            val newPath = "$bookNumber-${book.uppercase(Locale.US)}.usfm"
            if (inputAccessor.fileExists(usfmFile)) {
                inputAccessor.getInputStream(usfmFile).use { ifs ->
                    outputAccessor.write(newPath) {
                        ifs.transferTo(it)
                    }
                }
                newIngredientsByBook.put(book, usfmFiles)
            }
        }
        return newIngredientsByBook
    }

    internal fun createEmptyUsfmTemplate(bookNumber: Int, bookCode: String): String {
        return """
            \id ${bookCode.uppercase(Locale.US)}
            \c 1
            \p
        """.trimIndent()
    }


    internal fun moveAudioFiles(
        burrito: MetadataSchema,
        chapterAudioByBook: FilesByBook,
        outputAccessor: IResourceContainerAccessor
    ) {
        val (titleCode, _) = getTitleFromBurrito(burrito)
        val languageCode = getLanguageFromBurrito(burrito).identifier
        for ((book, filesByChapter) in chapterAudioByBook) {
            if (filesByChapter.isEmpty()) continue
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = mapBookNumberToUfwBookNumber((bookIndex))
            for ((chapter, audioFiles) in filesByChapter) {
                for (af in audioFiles) {
                    //val (audioFile, ingredient) = af
                    val extension = af.extension

                    val newPath = "media/${
                        getFilename(languageCode, titleCode, book, extension)
                            .replace("{chapter}", "$chapter")
                    }"
                    af.inputStream().use { ifs ->
                        outputAccessor.write(newPath) {
                            ifs.transferTo(it)
                        }
                    }
                }
            }
        }
    }
}

typealias MarkerLocation = Pair<AudioMarker, IntRange>

internal fun dublinCoreFromBurrito(burrito: MetadataSchema): DublinCore {
    val (identifier, title) = getTitleFromBurrito(burrito)
    return DublinCore(
        type = "bundle",
        conformsTo = "0.2",
        format = "text/usfm",
        identifier = identifier,
        title = title,
        creator = getCreatorFromBurrito(burrito),
        version = getVersionFromBurrito(burrito),
        description = getDescriptionFromBurrito(burrito),
        language = getLanguageFromBurrito(burrito),
        rights = getCopyrightFromBurrito(burrito),
        issued = getCreationDateFromBurrito(burrito),
        modified = LocalDateTime.now().toString()
    )
}

internal fun getCreationDateFromBurrito(burrito: MetadataSchema): String {
    return burrito
        .meta
        .dateCreated
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        .toString()
}

internal fun getCreatorFromBurrito(burrito: MetadataSchema): String {
    val defaultLocale = burrito.meta.defaultLocale
    val rightsHolder = burrito.agencies.find { it.roles.contains(Role.RIGHTS_HOLDER) }?.name?.short?.getOrDefault(defaultLocale, "")
    return if (!rightsHolder.isNullOrEmpty()) {
        rightsHolder
    } else {
        "unknown"
    }
}

internal fun getVersionFromBurrito(burrito: MetadataSchema): String {
    try {
        val version =
            burrito.identification?.primary?.entries?.first()?.value?.get("revision")?.toString()

        return if (!version.isNullOrEmpty()) {
            version
        } else {
            "latest"
        }
    } catch (e: Exception) {
        return "latest"
    }
}


internal fun getDescriptionFromBurrito(burrito: MetadataSchema): String {
    val langSlug = burrito.meta.defaultLocale
    var desc = ""
    burrito.identification?.let {
        desc = it.description["en"] ?: it.description[langSlug] ?: ""
    }
    return desc
}

internal fun getTitleFromBurrito(burrito: MetadataSchema): Pair<String, String> {
    val langSlug = burrito.meta.defaultLocale
    var slug = DEFAULT_TITLE_CODE
    var title = ""
    burrito.identification?.let {
        slug = it.abbreviation["en"] ?: it.abbreviation[langSlug] ?: ""
        title = it.name["en"] ?: it.name[langSlug] ?: ""
    }
    return Pair(slug, title)
}

internal fun getLanguageFromBurrito(burrito: MetadataSchema): Language {
    val slug = burrito.meta.defaultLocale
    val lang = burrito.languages.first()
    val direction = lang.scriptDirection?.value() ?: ""
    return Language(
        direction,
        slug,
        lang.name[slug] ?: lang.name["en"] ?: ""
    )
}

internal fun getCopyrightFromBurrito(burrito: MetadataSchema): String {
    return burrito
        .copyright
        .shortStatements
        .map { it.statement }
        .reduce { acc, shortStatement -> "$acc\n$shortStatement" }
}

internal fun getMarkersFromBurritoTimining(
    timingFile: File,
    audioFile: File
): List<AudioMarker> {
    return BurritoAlignmentMetadata(timingFile, audioFile)
        .parseTimings()
        .getMarkers()
}

internal fun findMatchingTimingFile(
    audioFile: String,
    ingredients: List<Pair<String, IngredientSchema>>,
    inputAccessor: IContainerAccessor
): Pair<String, IngredientSchema>? {
    // Begin looking for a timing file that matches the name exactly
    val matchedNameTiming = ingredients.find { (name, ingredient) ->
        val audioName = File(audioFile).nameWithoutExtension
        val timingName = File(name).nameWithoutExtension

        File(name).extension == "json" && audioName == timingName
    }

    if (matchedNameTiming != null) {
        return matchedNameTiming
    }

    // Exact match is not found so look for a timing file with the docid matching the audio file name
    for ((path, schema) in ingredients) {
        if (File(path).extension != "json") continue
        if (schema.role != "timing") continue

        inputAccessor.getInputStream(path).use {
            val timingFile = it.reader().readText()
            val timing = BurritoAudioAlignment.load(timingFile)
            if (timing.getAllDocids().contains(File(audioFile).name)) return Pair(path, schema)
        }
    }

    return null
}

internal fun filterAcceptedAudioFormats(
    burrito: MetadataSchema, ingedientsByBook: IngredientsByBook
): IngredientsByBook {
    val audioFlavor = (burrito.type!!.flavorType.flavor as AudioFlavorSchema)
    val approved = audioFlavor
        .getFormats()
        .filter { (formatName, format) ->
            val supported = format.compression in arrayOf(Compression.WAV, Compression.MP3)
            val validMp3 = validateMp3Format(format)
            val validWav = validateWavFormat(format)
            (supported && (validMp3 || validWav))
        }
    val approvedMimeType = approved.map { (name, format) ->
        when (format.compression) {
            Compression.MP3 -> "audio/mpeg"
            Compression.WAV -> "audio/wav"
            else -> throw Exception("Audio format ${format} not filtered out.")
        }
    }

    val accepted = HashMap<String, List<Pair<String, IngredientSchema>>>()
    ingedientsByBook.forEach { (book, ingredients) ->
        accepted[book] = ingredients.filter { (filename, ingredient) ->
            ingredient.mimeType in listOf(
                *approvedMimeType.toTypedArray(),
                "application/x-cue"
            ) ||
                    ingredient.role == "timing"
        }
    }
    return accepted
}

internal fun validateWavFormat(format: AudioFormat): Boolean {
    return arrayOf(
        format.compression == Compression.WAV,
        // don't fail if sampling rate or configuration are not provided
        format.samplingRate?.equals(DEFAULT_SAMPLE_RATE) ?: true,
        format.trackConfiguration?.equals(TrackConfiguration.MONO) ?: true,
        format.bitDepth?.equals(16) ?: true
    ).all { it }
}

internal fun validateMp3Format(format: AudioFormat): Boolean {
    return arrayOf(
        format.compression == Compression.MP3,
        // don't fail if sampling rate or configuration are not provided
        format.samplingRate?.equals(DEFAULT_SAMPLE_RATE) ?: true,
        format.trackConfiguration?.equals(TrackConfiguration.MONO) ?: true,
    ).all { it }
}

internal fun createMediaManifest(
    burrito: MetadataSchema,
    chapterAudioByBook: FilesByBook
): MediaManifest {
    val (titleCode, _) = getTitleFromBurrito(burrito)
    val languageCode = getLanguageFromBurrito(burrito).identifier
    val mediaProjects = chapterAudioByBook.map { (book, chapterIngredients) ->
        val audioEntries = setOf(
            *AudioFileFormat.extensions.toTypedArray(),
            *AudioMetadataFileFormat.extensions.toTypedArray()
        )
            .map { extension ->
                Media(
                    identifier = extension,
                    chapterUrl = "media/${
                        getFilename(
                            languageCode,
                            titleCode,
                            book,
                            extension
                        )
                    }"
                )
            }
        MediaProject(
            identifier = book,
            media = audioEntries
        )
    }
    return MediaManifest(projects = mediaProjects)
}

internal fun getVersification(
    burrito: MetadataSchema,
    usfmFilesByBook: Any,
    chapterAudioByBook: Any
): String {
    return "ufw"
}

internal fun getUSFMIngredients(ingedientsByBook: IngredientsByBook): IngredientsByBook {
    val usfmMimetypes = listOf("text/usfm", "text/usfm3")
    val filtered = HashMap<String, List<Pair<String, IngredientSchema>>>()
    ingedientsByBook.forEach { book, ingredientList ->
        val items = ingredientList.filter { (file, ingredient) ->
            File(file).extension == "usfm" || ingredient.mimeType in usfmMimetypes
        }
        filtered[book] = items
    }
    return filtered
}

internal fun createProjects(
    burrito: MetadataSchema,
    versification: String,
    bookSlugs: Iterable<String>,
    filenamePattern: String
): List<Project> {
    return bookSlugs.map { slug ->
        val usfmFile = filenamePattern
            .replace("{booknum}", "${getBookNumber(slug)}")
            .replace("{book}", slug.uppercase(Locale.US))
        Project(
            title = getBookTitle(burrito, slug),
            versification = versification,
            identifier = slug,
            sort = getBookNumber(slug),
            path = usfmFile,
            categories = listOf(getTestament(slug))
        )
    }
}

internal fun getBookTitle(burrito: MetadataSchema, bookSlug: String): String {
    val locale = burrito.meta.defaultLocale
    val localizedTitle = burrito.localizedNames["book-${bookSlug.lowercase(Locale.US)}"]
    localizedTitle?.let { localizedTitle ->
        return localizedTitle.short[locale] ?: localizedTitle.short["en"] ?: ""
    }
    return ""
}

internal fun getFilename(
    languageCode: String,
    titleCode: String,
    bookSlug: String,
    extension: String
): String {
    val titleCode = if (titleCode.isEmpty()) DEFAULT_TITLE_CODE else titleCode
    return filenamePattern
        .replace("{book}", bookSlug)
        .replace("{title}", titleCode)
        .replace("{language}", languageCode)
        .replace("{extension}", extension)
}

internal fun getIngredientsByBook(burrito: MetadataSchema): IngredientsByBook {
    val slugs = burrito.type!!.flavorType.currentScope.keys.map { it.lowercase(Locale.US) }
    val ingredientsByBook =
        slugs.associateWith { mutableListOf<Pair<String, IngredientSchema>>() }
    burrito.ingredients.forEach { filepath, item ->
        item.scope?.let { scope ->
            scope.keys.forEach {
                val slug = it.lowercase(Locale.US)
                ingredientsByBook[slug]?.add(Pair(filepath, item))
            }
        }
    }
    return ingredientsByBook
}