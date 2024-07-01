package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.container.BurritoContainer
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.TrackConfiguration
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
import java.util.*
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.collections.HashMap

internal typealias IngredientsByBook = Map<String, List<Pair<String, IngredientSchema>>>

internal val books = arrayOf(
    "gen", "exo", "lev", "num", "deu", "jos", "jdg", "rut", "1sa", "2sa", "1ki", "2ki", "1ch", "2ch",
    "ezr", "neh", "est", "job", "psa", "pro", "ecc", "sng", "isa", "jer", "lam", "ezk", "dan", "hos",
    "jol", "amo", "oba", "jon", "mic", "nam", "hab", "zep", "hag", "zec", "mal", "mat", "mrk", "luk",
    "jhn", "act", "rom", "1co", "2co", "gal", "eph", "php", "col", "1th", "2th", "1ti", "2ti", "tit",
    "phm", "heb", "jas", "1pe", "2pe", "1jn", "2jn", "3jn", "jud", "rev"
)
internal val ot = books.slice(0 until 41)
internal val nt = books.slice(41 until 66)

internal fun getBookSort(bookSlug: String): Int {
    return books.indexOf(bookSlug) + 1
}

internal fun getTestament(bookSlug: String): String {
    return when (bookSlug) {
        in ot -> "bible-ot"
        in nt -> "bible-nt"
        else -> ""
    }
}

class BurritoToResourceContainerConverter @Inject constructor() {

    private val usfmFilenamePattern = "./{booknum}-{book}.usfm"
    private val filenamePattern = "{language}_{title}_{book}_c{chapter}.{extension}"
    private val DEFAULT_TITLE_CODE = "reg"

    fun convert(
        burrito: File,
        outputFile: File
    ): Boolean {
        if (outputFile.extension == "zip") outputFile.outputStream().use { ZipOutputStream(it).use { } }
        val burrito = BurritoContainer.load(burrito)
        burrito.use {
            val metadata = it.manifest
            ResourceContainer.create(outputFile) {
                val (projects, media) = processContentInBurrito(metadata, burrito.accessor, this.accessor)
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

    private fun dublinCoreFromBurrito(burrito: MetadataSchema): DublinCore {
        val (identifier, title) = getTitleFromBurrito(burrito)
        return DublinCore(
            type = "bundle",
            conformsTo = "0.2",
            format = "text/usfm",
            identifier = identifier,
            title = title,
            description = getDescriptionFromBurrito(burrito),
            language = getLanguageFromBurrito(burrito),
            rights = getCopyrightFromBurrito(burrito),
            issued = getCreationDateFromBurrito(burrito),
            modified = LocalDateTime.now().toString()
        )
    }

    private fun getCreationDateFromBurrito(burrito: MetadataSchema): String {
        return burrito
            .meta
            .dateCreated
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .toString()
    }

    private fun getDescriptionFromBurrito(burrito: MetadataSchema): String {

        val langSlug = burrito.meta.defaultLocale
        var desc = ""
        burrito.identification?.let {
            desc = it.description["en"] ?: it.description[langSlug] ?: ""
        }
        return desc
    }

    private fun getTitleFromBurrito(burrito: MetadataSchema): Pair<String, String> {
        val langSlug = burrito.meta.defaultLocale
        var slug = DEFAULT_TITLE_CODE
        var title = ""
        burrito.identification?.let {
            slug = it.abbreviation["en"] ?: it.abbreviation[langSlug] ?: ""
            title = it.name["en"] ?: it.name[langSlug] ?: ""
        }
        return Pair(slug, title)
    }

    private fun getLanguageFromBurrito(burrito: MetadataSchema): Language {
        val slug = burrito.meta.defaultLocale
        val lang = burrito.languages.first { it.tag == slug }
        val direction = lang.scriptDirection?.value() ?: ""
        return Language(
            direction,
            slug,
            lang.name[slug] ?: lang.name["en"] ?: ""
        )
    }

    private fun getCopyrightFromBurrito(burrito: MetadataSchema): String {
        return burrito
            .copyright
            .shortStatements
            .map { it.statement }
            .reduce { acc, shortStatement -> "$acc\n$shortStatement" }
    }

    private fun processContentInBurrito(
        burrito: MetadataSchema,
        inputAccessor: IContainerAccessor,
        outputAccessor: IResourceContainerAccessor
    ): Pair<List<Project>, MediaManifest> {
        val ingredientsByBook = getIngredientsByBook(burrito)
        val usfmFilesByBook = getUSFMIngredients(ingredientsByBook)
        val chapterAudioByBook = createChapterAudioIngredients(burrito, ingredientsByBook)

        val versification = getVersification(burrito, usfmFilesByBook, chapterAudioByBook)

        moveUSFMFiles(burrito, usfmFilesByBook, inputAccessor, outputAccessor)
        moveAudioFiles(burrito, chapterAudioByBook, inputAccessor, outputAccessor)

        val mediaManifest = createMediaManifest(burrito, chapterAudioByBook)
        val projects = createProjects(
            burrito,
            versification,
            ingredientsByBook.keys,
            usfmFilenamePattern
        )

        return Pair(projects, mediaManifest)
    }

    private fun createChapterAudioIngredients(
        burrito: MetadataSchema,
        ingredientsByBook: IngredientsByBook
    ): IngredientsByBook {
        val filtered = filterAcceptedAudioFormats(burrito, ingredientsByBook)
        val reconstructed = hashMapOf<String, MutableList<Pair<String, IngredientSchema>>>()
        for ((book, ingredients) in filtered) {
            val groupedByChapter = hashMapOf<Int, MutableList<Pair<String, IngredientSchema>>>()
            ingredients.forEach { item ->
                val (file, ingredient) = item
                val scope = ingredient.scope?.get(book.uppercase(Locale.US))!!
                when {
                    scope.isEmpty() -> {
                        assert(false)
                        // breakBookAudioIntoChapters()
                    }

                    scope.size == 1 -> {
                        val chapterNumber = scope.single().toInt()
                        if (groupedByChapter.containsKey(chapterNumber)) {
                            groupedByChapter[chapterNumber]!!.add(item)
                        } else {
                            groupedByChapter[chapterNumber] = mutableListOf(item)
                        }
                    }

                    scope.size > 1 -> {
                        assert(false)
                        // combineSubchapterIntoChapter()
                    }
                }
            }
            reconstructed[book] = groupedByChapter.values.flatten().toMutableList()
        }
        return reconstructed
    }

    private fun filterAcceptedAudioFormats(
        burrito: MetadataSchema, ingedientsByBook: IngredientsByBook
    ): IngredientsByBook {
        val audioFlavor = (burrito.type!!.flavorType.flavor as AudioFlavorSchema)
        val approved = audioFlavor
            .getFormats()
            .filter { (formatName, format) ->
                val supported = format.compression in arrayOf(Compression.WAV, Compression.MP3)
                val validMp3 = validateMp3Format(format)
                val validWav = validateWavFormat(format)
                supported && (validMp3 || validWav)
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
                ingredient.mimeType in listOf(*approvedMimeType.toTypedArray(), "application/x-cue")
            }
        }
        return accepted
    }

    private fun validateWavFormat(format: AudioFormat): Boolean {
        return arrayOf(
            format.compression == Compression.WAV,
            // don't fail if sampling rate or configuration are not provided
            format.samplingRate?.equals(44100) ?: true,
            format.trackConfiguration?.equals(TrackConfiguration.MONO) ?: true,
            format.bitDepth?.equals(16) ?: true
        ).all { it }
    }

    private fun validateMp3Format(format: AudioFormat): Boolean {
        return arrayOf(
            format.compression == Compression.MP3,
            // don't fail if sampling rate or configuration are not provided
            format.samplingRate?.equals(44100) ?: true,
            format.trackConfiguration?.equals(TrackConfiguration.MONO) ?: true,
        ).all { it }
    }

    private fun createMediaManifest(
        burrito: MetadataSchema,
        chapterAudioByBook: IngredientsByBook
    ): MediaManifest {
        val (titleCode, _) = getTitleFromBurrito(burrito)
        val languageCode = getLanguageFromBurrito(burrito).identifier
        return MediaManifest(
            projects = chapterAudioByBook.map { (book, chapterIngredients) ->
                val audioEntries = chapterIngredients
                    .map { (chapterFile, _) ->
                        File(chapterFile).extension
                    }
                    .toSet()
                    .map { extension ->
                        Media(
                            identifier = extension,
                            chapterUrl = "media/${getFilename(languageCode, titleCode, book, extension)}"
                        )
                    }
                MediaProject(
                    identifier = book,
                    media = audioEntries
                )
            }
        )
    }

    private fun moveUSFMFiles(
        burrito: MetadataSchema,
        usfmFilesByBook: IngredientsByBook,
        inputAccessor: IContainerAccessor,
        outputAccessor: IResourceContainerAccessor
    ) {
        for ((book, usfmFiles) in usfmFilesByBook) {
            if (usfmFiles.isEmpty()) continue
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = if (bookIndex <= 38) bookIndex + 1 else bookIndex + 2
            val (usfmFile, ingredient) = usfmFiles.first()
            if (inputAccessor.fileExists(usfmFile)) {
                val newPath = "$bookNumber-${book.uppercase(Locale.US)}.usfm"
                val ifs = inputAccessor.getInputStream(usfmFile)
                outputAccessor.write(newPath) {
                    ifs.transferTo(it)
                }
            }
        }
    }

    private fun moveAudioFiles(
        burrito: MetadataSchema,
        usfmFilesByBook: IngredientsByBook,
        inputAccessor: IContainerAccessor,
        outputAccessor: IResourceContainerAccessor
    ) {
        val (titleCode, _) = getTitleFromBurrito(burrito)
        val languageCode = getLanguageFromBurrito(burrito).identifier
        for ((book, audioFiles) in usfmFilesByBook) {
            if (audioFiles.isEmpty()) continue
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = if (bookIndex <= 38) bookIndex + 1 else bookIndex + 2
            for (af in audioFiles) {
                val (audioFile, ingredient) = af
                val chapter = ingredient!!.scope?.get(book.uppercase(Locale.US))?.single()!!
                val extension = File(audioFile).extension
                if (inputAccessor.fileExists(audioFile)) {
                    val newPath = "media/${
                        getFilename(languageCode, titleCode, book, extension)
                            .replace("{chapter}", chapter)
                    }"
                    val ifs = inputAccessor.getInputStream(audioFile)
                    outputAccessor.write(newPath) {
                        ifs.transferTo(it)
                    }
                }
            }
        }
    }

    private fun getVersification(
        burrito: MetadataSchema,
        usfmFilesByBook: Any,
        chapterAudioByBook: Any
    ): String {
        return "ufw"
    }

    private fun getUSFMIngredients(ingedientsByBook: IngredientsByBook): IngredientsByBook {
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

    private fun createProjects(
        burrito: MetadataSchema,
        versification: String,
        bookSlugs: Iterable<String>,
        filenamePattern: String
    ): List<Project> {
        return bookSlugs.map { slug ->
            val usfmFile = filenamePattern
                .replace("{booknum}", "${getBookSort(slug)}")
                .replace("{book}", slug.uppercase(Locale.US))
            Project(
                title = getBookTitle(burrito, slug),
                versification = versification,
                identifier = slug,
                sort = getBookSort(slug),
                path = usfmFile,
                categories = listOf(getTestament(slug))
            )
        }
    }

    private fun getBookTitle(burrito: MetadataSchema, bookSlug: String): String {
        val locale = burrito.meta.defaultLocale
        val localizedTitle = burrito.localizedNames["book-${bookSlug.lowercase(Locale.US)}"]
        localizedTitle?.let { localizedTitle ->
            return localizedTitle.short[locale] ?: localizedTitle.short["en"] ?: ""
        }
        return ""
    }

    private fun getFilename(languageCode: String, titleCode: String, bookSlug: String, extension: String): String {
        val titleCode = if (titleCode.isEmpty()) DEFAULT_TITLE_CODE else titleCode
        return filenamePattern
            .replace("{book}", bookSlug)
            .replace("{title}", titleCode)
            .replace("{language}", languageCode)
            .replace("{extension}", extension)
    }

    private fun getIngredientsByBook(burrito: MetadataSchema): IngredientsByBook {
        val slugs = burrito.type!!.flavorType.currentScope.keys.map { it.lowercase(Locale.US) }
        val ingredientsByBook = slugs.associateWith { mutableListOf<Pair<String, IngredientSchema>>() }
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
}