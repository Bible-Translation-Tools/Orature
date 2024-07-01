package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.scriptureburrito.Checksum
import org.bibletranslationtools.scriptureburrito.CopyrightSchema
import org.bibletranslationtools.scriptureburrito.Flavor
import org.bibletranslationtools.scriptureburrito.Format
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.IngredientsSchema
import org.bibletranslationtools.scriptureburrito.LanguageSchema
import org.bibletranslationtools.scriptureburrito.Languages
import org.bibletranslationtools.scriptureburrito.LocalizedNamesSchema
import org.bibletranslationtools.scriptureburrito.LocalizedText
import org.bibletranslationtools.scriptureburrito.MetaVersionSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.ScopeSchema
import org.bibletranslationtools.scriptureburrito.ShortStatement
import org.bibletranslationtools.scriptureburrito.SoftwareAndUserInfoSchema
import org.bibletranslationtools.scriptureburrito.SourceMetaSchema
import org.bibletranslationtools.scriptureburrito.SourceMetadataSchema
import org.bibletranslationtools.scriptureburrito.TypeSchema
import org.bibletranslationtools.scriptureburrito.flavor.FlavorType
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFlavorSchema
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.AudioFormat
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Compression
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Formats
import org.bibletranslationtools.scriptureburrito.flavor.scripture.audio.Performance
import org.wycliffeassociates.otter.common.data.IAppInfo
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth.AuthProvider
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.inject.Inject

typealias ChapterNumber = Int

class ScriptureBurritoUtils @Inject constructor(
    private val idAuthorityProvider: AuthProvider,
    private val appInfo: IAppInfo,
    directoryProvider: IDirectoryProvider
) {

    private val tempDir = directoryProvider.tempDirectory
    private val mapper = ObjectMapper()
    private val appName = appInfo.appName
    private val appVersion = appInfo.appVersion

    init {
        mapper.registerKotlinModule()
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        mapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
    }

    fun writeBurritoManifest(
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        rc: ResourceContainer,
        outputStream: OutputStream
    ) {
        val manifest = createBurritoManifest(
            workbook,
            takes,
            rc
        )

        outputStream.use {
            mapper.writeValue(it, manifest)
        }
    }

    fun createBurritoManifest(
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        rc: ResourceContainer
    ): MetadataSchema {
        val language = workbook.target.language
        val langCode = language.slug

        return SourceMetadataSchema(
            Format.SCRIPTURE_BURRITO,
            SourceMetaSchema(
                dateCreated = Date.from(Instant.now()),
                version = MetaVersionSchema._1_0_0,
                defaultLocale = rc.manifest.dublinCore.language.identifier,
                generator = SoftwareAndUserInfoSchema().apply {
                    softwareName = appName
                    softwareVersion = appVersion
                }
            ),
            idAuthorityProvider.createIdAuthority(),
            idAuthorityProvider.createIdentification().apply {
                this.name["en"] = workbook.target.resourceMetadata.title
                this.abbreviation["en"] = workbook.target.resourceMetadata.identifier
            },
            confidential = false,
            copyright = CopyrightSchema().apply {
                this.shortStatements =
                    mutableListOf(ShortStatement(rc.manifest.dublinCore.rights, langCode))
            },
            type = TypeSchema(
                FlavorType(
                    name = Flavor.SCRIPTURE,
                    AudioFlavorSchema(
                        mutableSetOf(Performance.READING, Performance.SINGLE_VOICE),
                        formats = Formats().apply {
                            put("format-wav", AudioFormat(Compression.WAV))
                            put("format-mp3", AudioFormat(Compression.MP3))
                        }
                    ),
                    currentScope = ScopeSchema().apply {
                        this[workbook.target.slug.uppercase(Locale.US)] =
                            takes.keys.map { "$it" }.toMutableList()
                    }
                )
            ),
            languages = Languages().apply {
                add(
                    LanguageSchema(
                        tag = language.slug,
                        name = hashMapOf(
                            language.slug to language.name,
                            "en" to language.anglicizedName
                        )
                    )
                )
            },
            localizedNames = buildLocalizedNames(rc),
            ingredients = buildIngredients(rc, workbook, takes)
        )
    }

    private fun buildIngredients(
        rc: ResourceContainer,
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>
    ): IngredientsSchema {
        val ingredients = IngredientsSchema()
        val files = mutableMapOf<String, File>()
        val outTempDir = File(tempDir, "burritoDir").apply { mkdirs() }
        val usfmFiles = rc.manifest.projects.map { project ->
            if (project.path.contains(".usfm")) {
                val path = "${project.path.removePrefix("./")}"
                val bookDir = File(outTempDir, "${project.identifier}").mkdirs()
                val outFile = File(outTempDir, path).apply { createNewFile() }

                rc.accessor
                    .getInputStream(project.path.removePrefix("./")).use { ifs ->
                        outFile.outputStream().use { ofs ->
                            ifs.transferTo(ofs)
                        }
                    }

                files["${project.identifier}/$path"] = outFile
                val ingredient = IngredientSchema().apply {
                    this.mimeType = "text/usfm"
                    this.size = outFile.length().toInt()
                    this.checksum = Checksum().apply {
                        this.md5 = calculateMD5(outFile)
                    }
                    this.scope = ScopeSchema().apply {
                        put(
                            project.identifier.uppercase(Locale.US),
                            mutableListOf()
                        )
                    }

                }
                ingredients["$path"] = ingredient
            }
        }
        val book = workbook.target.slug
        takes.forEach { (chapterNumber, audioFile) ->
            for (take in audioFile) {
                val path = "${RcConstants.SOURCE_MEDIA_DIR}/${take.name}"
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
                    scope = ScopeSchema().apply {
                        put(
                            book.uppercase(Locale.US),
                            mutableListOf("$chapterNumber")
                        )
                    }
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
}