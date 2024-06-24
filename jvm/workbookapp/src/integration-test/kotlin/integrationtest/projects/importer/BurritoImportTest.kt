package integrationtest.projects.importer

import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.container.BurritoContainer
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.TrackConfiguration
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.*
import java.io.File
import java.util.*
import kotlin.collections.HashMap

const val filenamePattern = "media/{language}_{title}_{book}_c{chapter}.{extension}"

class BurritoImportTest {

    val burrito = File("/Users/joe/Desktop/test.burrito")
    val rc = File("/Users/joe/Desktop/test.rc")

    @Test
    fun testOpenBurrito() {
        val burrito = BurritoContainer.load(File("/Users/joe/Desktop/test.burrito"))
        val metadata = burrito.manifest
        println(metadata)

        ResourceContainer.create(rc) {
            val (projects, media) = processContentInBurrito(metadata, this)
            this.manifest = Manifest(
                dublinCore = dublinCoreFromBurrito(metadata),
                projects = projects,
                checking = Checking(),
            )
            this.media = media
        }
    }

    private fun dublinCoreFromBurrito(burrito: MetadataSchema): DublinCore {
        val (identifier, title) = getTitleFromBurrito(burrito)
        return DublinCore(
            type = "bundle",
            conformsTo = "0.2",
            format = "text/usfm",
            identifier = identifier,
            title = title,
            description = "",
            language = getLanguageFromBurrito(burrito),
            rights = getCopyrightFromBurrito(burrito),
        )
    }

    private fun getTitleFromBurrito(burrito: MetadataSchema): Pair<String, String> {
        val langSlug = burrito.meta.defaultLocale
        var slug = ""
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
        return Language(
            "",
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
        resourceContainer: ResourceContainer
    ): Pair<List<Project>, MediaManifest> {
        val ingredientsByBook = getIngredientsByBook(burrito)
        val usfmFilesByBook = getUSFMIngredients(ingredientsByBook)
        val chapterAudioByBook = createChapterAudioIngredients(burrito, ingredientsByBook)

        val versification = getVersification(burrito, usfmFilesByBook, chapterAudioByBook)

        moveUSFMFiles(burrito, usfmFilesByBook, resourceContainer)
        moveAudioFiles(burrito, chapterAudioByBook, resourceContainer)

        val mediaManifest = createMediaManifest(burrito, chapterAudioByBook)
        val projects = createProjects(
            burrito,
            versification,
            ingredientsByBook.keys,
            filenamePattern
        )

        return Pair(projects, mediaManifest)
    }

    private fun createChapterAudioIngredients(
        burrito: MetadataSchema,
        ingredientsByBook: IngredientsByBook
    ): IngredientsByBook {
        val filtered = filterAcceptedAudioFormats(burrito, ingredientsByBook)
        val reconstructed = hashMapOf<String, List<Pair<String, IngredientSchema>>>()
        for ((book, ingredients) in filtered) {
            val groupedByChapter = hashMapOf<Int, List<Pair<String, IngredientSchema>>>()
            ingredients.forEach { item ->
                val (file, ingredient) = item
                val scope = ingredient.scope?.get(book.uppercase(Locale.US))!!
                when {
                    scope.isEmpty() -> {
                        assert(false)
                        // breakBookAudioIntoChapters()
                    }
                    scope.size == 1 -> {
                        groupedByChapter[scope.single().toInt()] = listOf(item)
                    }
                    scope.size > 1 -> {
                        assert(false)
                        // combineSubchapterIntoChapter()
                    }
                }
            }
            reconstructed[book] = groupedByChapter.values.flatten()
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
                ingredient.mimeType in approvedMimeType
            }
        }
        return accepted
    }

    private fun validateWavFormat(format: AudioFormat): Boolean {
        return arrayOf(
            format.samplingRate == 44100,
            format.compression == Compression.WAV,
            format.trackConfiguration == TrackConfiguration.MONO,
            format.bitDepth == 16
        ).all { it }
    }

    private fun validateMp3Format(format: AudioFormat): Boolean {
        return arrayOf(
            format.samplingRate == 44100,
            format.compression == Compression.MP3,
            format.trackConfiguration == TrackConfiguration.MONO,
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
                val audioEntries = chapterIngredients.map { (chapterFile, _) ->
                    val extension = File(chapterFile).extension
                    Media(
                        identifier = extension,
                        chapterUrl = getFilename(languageCode, titleCode, book, extension)
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
        rc: ResourceContainer
    ) {
        for ((book, usfmFiles) in usfmFilesByBook) {
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = if (bookIndex <= 38) bookIndex + 1 else bookIndex + 2
            val (usfmFile, ingredient) = usfmFiles.first()
            if (rc.accessor.fileExists(usfmFile)) {
                val newPath = "$bookNumber-${book.uppercase(Locale.US)}.usfm"
                val ifs = rc.accessor.getInputStream(usfmFile)
                rc.accessor.write(newPath) {
                    ifs.transferTo(it)
                }
            }
        }
    }

    private fun moveAudioFiles(
        burrito: MetadataSchema,
        usfmFilesByBook: IngredientsByBook,
        rc: ResourceContainer
    ) {
        val (titleCode, _) = getTitleFromBurrito(burrito)
        val languageCode = getLanguageFromBurrito(burrito).identifier
        for ((book, audioFiles) in usfmFilesByBook) {
            val bookIndex = books.indexOf(book.lowercase(Locale.US))
            // NT starts at 41
            val bookNumber = if (bookIndex <= 38) bookIndex + 1 else bookIndex + 2
            val (audioFile, ingredient) = audioFiles.first()
            val chapter = ingredient!!.scope?.get(book.uppercase(Locale.US))?.single()!!
            val extension = File(audioFile).extension
            if (rc.accessor.fileExists(audioFile)) {
                val newPath = "media/${
                    getFilename(languageCode, titleCode, book, extension)
                        .replace("{chapter}", chapter)
                }.$extension"
                val ifs = rc.accessor.getInputStream(audioFile)
                rc.accessor.write(newPath) {
                    ifs.transferTo(it)
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
                File(file).extension == ".usfm" || ingredient.mimeType in usfmMimetypes
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
        val (titleCode, _) = getTitleFromBurrito(burrito)
        val languageCode = getLanguageFromBurrito(burrito).identifier
        return bookSlugs.map { slug ->
            Project(
                title = getBookTitle(burrito, slug),
                versification = versification,
                identifier = slug,
                sort = getBookSort(slug),
                path = getFilename(languageCode, titleCode, slug, "usfm"),
                categories = listOf(getTestament(slug))
            )
        }
    }

    private fun getBookTitle(burrito: MetadataSchema, bookSlug: String): String {
        val locale = burrito.meta.defaultLocale
        val localizedTitle = burrito.localizedNames["book-${bookSlug.lowercase(Locale.US)}"]
        localizedTitle?.let { localizedTitle ->
            return localizedTitle.long[locale] ?: localizedTitle.long["en"] ?: ""
        }
        return ""
    }

    private fun getFilename(languageCode: String, titleCode: String, bookSlug: String, extension: String): String {
        return filenamePattern
            .replace("{book}", bookSlug)
            .replace("{title}", titleCode)
            .replace("{language}", languageCode)
            .replace("{extension}", extension)
    }

    private fun getIngredientsByBook(burrito: MetadataSchema): Map<String, List<Pair<String, IngredientSchema>>> {
        val slugs = burrito.type!!.flavorType.currentScope.keys.map { it.lowercase(Locale.US) }
        val ingredientsByBook = slugs.associateWith { mutableListOf<Pair<String, IngredientSchema>>() }
        burrito.ingredients.forEach { filepath, item ->
            item.scope?.let { scope ->
                scope.keys.forEach {
                    val slug = it.lowercase(Locale.US)
                    ingredientsByBook[slug]!!.add(Pair(filepath, item))
                }
            }
        }
        return ingredientsByBook
    }
}

typealias IngredientsByBook = Map<String, List<Pair<String, IngredientSchema>>>

val books = arrayOf(
    "gen", "exo", "lev", "num", "deu", "jos", "jdg", "rut", "1sa", "2sa", "1ki", "2ki", "1ch", "2ch",
    "ezr", "neh", "est", "job", "psa", "pro", "ecc", "sng", "isa", "jer", "lam", "ezk", "dan", "hos",
    "jol", "amo", "oba", "jon", "mic", "nam", "hab", "zep", "hag", "zec", "mal", "mat", "mrk", "luk",
    "jhn", "act", "rom", "1co", "2co", "gal", "eph", "php", "col", "1th", "2th", "1ti", "2ti", "tit",
    "phm", "heb", "jas", "1pe", "2pe", "1jn", "2jn", "3jn", "jud", "rev"
)
val ot = books.slice(0..41)
val nt = books.slice(42..66)

fun getBookSort(bookSlug: String): Int {
    return books.indexOf(bookSlug) + 1
}

fun getTestament(bookSlug: String): String {
    return when (bookSlug) {
        in ot -> "bible-ot"
        in nt -> "bible-nt"
        else -> ""
    }
}