package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.Checksum
import org.bibletranslationtools.scriptureburrito.CopyrightSchema
import org.bibletranslationtools.scriptureburrito.DerivedMetaSchema
import org.bibletranslationtools.scriptureburrito.DerivedMetadataSchema
import org.bibletranslationtools.scriptureburrito.Flavor
import org.bibletranslationtools.scriptureburrito.Format
import org.bibletranslationtools.scriptureburrito.IdAuthoritiesSchema
import org.bibletranslationtools.scriptureburrito.IdAuthority
import org.bibletranslationtools.scriptureburrito.IdentificationSchema
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.IngredientsSchema
import org.bibletranslationtools.scriptureburrito.LanguageSchema
import org.bibletranslationtools.scriptureburrito.Languages
import org.bibletranslationtools.scriptureburrito.LocalizedNamesSchema
import org.bibletranslationtools.scriptureburrito.LocalizedText
import org.bibletranslationtools.scriptureburrito.MetaVersionSchema
import org.bibletranslationtools.scriptureburrito.MetadataDeserializer
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.ShortStatement
import org.bibletranslationtools.scriptureburrito.SoftwareAndUserInfoSchema
import org.bibletranslationtools.scriptureburrito.TypeSchema
import org.bibletranslationtools.scriptureburrito.flavor.FlavorType
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Formats
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Performance
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

typealias ChapterNumber = Int

object ScriptureBurritoUtils {

    fun writeBurritoManifest(
        appName: String,
        appVersion: String,
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        rc: ResourceContainer,
        language: Language,
        tempDir: File,
        outputStream: OutputStream
    ) {
        val manifest = createBurritoManifest(
            appName,
            appVersion,
            workbook,
            takes,
            rc,
            language,
            tempDir
        )

        val mapper = ObjectMapper()
        mapper.registerKotlinModule()
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        mapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
        outputStream.use {
            mapper.writeValue(it, manifest)
        }
    }

    fun createBurritoManifest(
        appName: String,
        appVersion: String,
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        rc: ResourceContainer,
        language: Language,
        tempDir: File
    ): MetadataSchema {
        val langCode = language.slug

        return DerivedMetadataSchema(
            Format.SCRIPTURE_BURRITO,
            DerivedMetaSchema(
                dateCreated = Date.from(Instant.now()),
                version = MetaVersionSchema("1.0.0"),
                defaultLocale = rc.manifest.dublinCore.language.identifier,
                generator = SoftwareAndUserInfoSchema().apply {
                    softwareName = appName
                    softwareVersion = appVersion
                }
            ),
            createWAIdAuthority(),
            IdentificationSchema(),
            confidential = false,
            copyright = CopyrightSchema().apply {
                this.shortStatements = listOf(ShortStatement(rc.manifest.dublinCore.rights, langCode))
            },
            type = TypeSchema(
                FlavorType(Flavor.SCRIPTURE).apply {
                    val formats = Formats()
                    formats.put("format-wav", AudioFormat(Compression.WAV))
                    formats.put("format-mp3", AudioFormat(Compression.MP3))
                    this.flavor = AudioFlavorSchema(
                        setOf(Performance.READING, Performance.SINGLE_VOICE),
                        formats
                    ).apply {
                        name = "audioTranslation"
                    }
                }
            ),
            languages = Languages().apply {
                add(
                    LanguageSchema().apply {
                        tag = language.slug

                    }
                )
            },
            localizedNames = buildLocalizedNames(rc),
            ingredients = buildIngredients(rc, workbook, takes, tempDir)
        )
    }

    private fun buildIngredients(
        rc: ResourceContainer,
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        tempDir: File
    ): IngredientsSchema {
        val ingredients = IngredientsSchema()
        val files = mutableMapOf<String, File>()
        val outTempDir = File(tempDir, "burritoDir").apply { mkdirs() }
        val usfmFiles = rc.manifest.projects.map {
            if (it.path.contains(".usfm")) {
                val path = "${it.path.removePrefix("./")}"
                val bookDir = File(outTempDir, "${it.identifier}").mkdirs()
                val outFile = File(outTempDir, path).apply { createNewFile() }
                rc.accessor.getInputStream(it.path.removePrefix("./")).transferTo(outFile.outputStream())
                files["${it.identifier}/$path"] = outFile
                val ingredient = IngredientSchema().apply {
                    this.mimeType = "text/usfm"
                    this.size = outFile.length().toInt()
                    this.checksum = Checksum().apply {
                        this.md5 = calculateMD5(outFile)
                    }
                }
                ingredients["$path"] = ingredient
            }
        }
        val book = workbook.target.slug
        takes.forEach { (chapterNumber, audioFile) ->
            for (take in audioFile) {
                val path = "${RcConstants.MEDIA_DIR}/${take.name}"
                val outFile = File(outTempDir, path).apply { parentFile.mkdirs() }
                files[path] = outFile
                val ingredient = IngredientSchema().apply {
                    this.mimeType = when (outFile.extension) {
                        "wav" -> "audio/wav"
                        "mp3" -> "audio/mpeg"
                        "cue" -> "application/x-cue"
                        else -> "application/octet-stream"
                    }
                    this.size = take.length().toInt()
                    this.checksum = Checksum().apply {
                        this.md5 = calculateMD5(take)
                    }
                    scope = mutableMapOf(book.uppercase(Locale.US) to mutableListOf("$chapterNumber"))
                }
                ingredients[path] = ingredient
            }
        }

        return ingredients
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun calculateMD5(file: File): String {
        val data = file.readBytes()
        val md = MessageDigest.getInstance("MD5")
        md.update(data)
        val digest = md.digest()
        val sb = StringBuilder()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun buildLocalizedNames(rc: ResourceContainer): LocalizedNamesSchema {
        val langCode = rc.manifest.dublinCore.language.identifier
        val names = LocalizedNamesSchema()

        rc.manifest.projects.forEach {
            val key = "book-${it.identifier}"
            if (names.containsKey(key)) {
                if (!names[key]!!.short.containsKey(langCode)) {
                    names[key]!!.short[langCode] = it.title
                }
            } else {
                names[key] = LocalizedText(
                    short = hashMapOf(
                        langCode to it.title
                    )
                )
            }
        }

        return names
    }

    private fun createWAIdAuthority(): IdAuthoritiesSchema {
        val authority = IdAuthoritiesSchema()
        val biel = IdAuthority()
        biel.name = hashMapOf("en" to "Bible In Every Language")
        biel.id = "https://www.bibleineverylanguage.org"
        authority["biel"] = biel
        return authority
    }
}