/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
import org.bibletranslationtools.scriptureburrito.flavor.scripture.text.TextTranslationSchema
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioMetadataFileFormat
import org.wycliffeassociates.otter.common.data.IAppInfo
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioConverter
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.audio.WAV_TO_MP3_COMPRESSED_RATE
import org.wycliffeassociates.otter.common.domain.audio.metadata.BurritoAlignmentMetadata
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.IProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ProjectExporterCallback
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth.AuthProvider
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.ZipAccessor
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

class BurritoWrapperExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val idAuthorityProvider: AuthProvider,
    private val appInfo: IAppInfo
) : IProjectExporter {

    @Inject
    lateinit var audioExporter: AudioExporter

    @Inject
    lateinit var audioConverter: AudioConverter

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper().apply {
        registerKotlinModule()
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
        dateFormat = SimpleDateFormat("yyyy-MM-dd")
    }
    private val appName = appInfo.appName
    private val appVersion = appInfo.appVersion

    override fun export(
        outputDirectory: File,
        workbook: Workbook,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        return Single.fromCallable {
            val projectSourceMetadata = workbook.source.linkedResources
                .firstOrNull { it.identifier == workbook.target.resourceMetadata.identifier }
                ?: workbook.source.resourceMetadata

            val projectAccessor = workbook.projectFilesAccessor
            if (!projectAccessor.isInitialized()) {
                return@fromCallable ExportResult.FAILURE
            }

            val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
            val targetZip = outputDirectory.resolve(zipFilename)

            logger.info("Exporting project as burrito wrapper: ${targetZip.nameWithoutExtension}")

            callback?.onNotifyProgress(10.0, "preparingExport")

            // Create temporary directory for burrito wrapper
            val tempWrapperDir = File(directoryProvider.tempDirectory, "burrito_wrapper_${Date().time}")
            tempWrapperDir.mkdirs()

            try {
                // Create text burrito directory first
                val textBurritoDir = File(tempWrapperDir, "text")
                textBurritoDir.mkdirs()

                // Get USFM files from source and copy directly to text burrito
                val sourceRCFile = workbook.source.resourceMetadata.path
                val usfmFiles = mutableListOf<Pair<String, File>>()
                val rcInfo = ResourceContainer.load(sourceRCFile).use { rc ->
                    val projects = rc.manifest.projects
                    val dublinCore = rc.manifest.dublinCore
                    val localizedNames = LocalizedNamesSchema()
                    
                    projects.forEach { project ->
                        if (project.path.contains(".usfm")) {
                            val path = project.path.removePrefix("./")
                            val targetUsfmFile = File(textBurritoDir, path)
                            targetUsfmFile.parentFile.mkdirs()
                            
                            rc.accessor.getInputStream(path).use { inputStream ->
                                targetUsfmFile.outputStream().use { outputStream ->
                                    inputStream.transferTo(outputStream)
                                }
                            }
                            usfmFiles.add(Pair(path, targetUsfmFile))
                            
                            // Build localized names
                            val key = "book-${project.identifier}"
                            localizedNames[key] = LocalizedText(
                                short = hashMapOf(
                                    dublinCore.language.identifier to project.title
                                )
                            )
                        }
                    }
                    
                    Triple(projects, dublinCore, localizedNames)
                }

                callback?.onNotifyProgress(30.0, "creatingTextBurrito")

                // Create text burrito metadata
                val textMetadata = createTextBurritoMetadata(workbook, rcInfo, usfmFiles)
                writeBurritoMetadata(textMetadata, textBurritoDir)

                callback?.onNotifyProgress(50.0, "gatheringAudio")

                // Get audio files
                val takes = gatherAudioFiles(workbook, options?.chapters)
                
                callback?.onNotifyProgress(70.0, "creatingAudioBurrito")

                // Create audio burrito
                val audioBurritoDir = File(tempWrapperDir, "audio")
                audioBurritoDir.mkdirs()
                
                // Convert audio files to the desired format (default MP3)
                val audioFormat = options?.audioFormat ?: AudioFileFormat.MP3
                val convertedTakes = convertAudioFiles(workbook, takes, audioFormat, callback)
                
                // Copy audio files first so timing files can be created
                copyAudioFilesToBurrito(workbook, convertedTakes, audioBurritoDir, audioFormat)
                val audioMetadata = createAudioBurritoMetadata(workbook, convertedTakes, audioBurritoDir)
                writeBurritoMetadata(audioMetadata, audioBurritoDir)

                callback?.onNotifyProgress(90.0, "creatingWrapper")

                // Create wrapper metadata
                val wrapperMetadata = createWrapperMetadata(workbook)
                writeWrapperMetadata(wrapperMetadata, tempWrapperDir)

                // Create zip file
                createWrapperZip(tempWrapperDir, targetZip)

                callback?.onNotifyProgress(100.0)
                callback?.onNotifySuccess(workbook.target.toCollection(), targetZip)

                ExportResult.SUCCESS
            } catch (e: Exception) {
                logger.error("Error while exporting project as burrito wrapper", e)
                callback?.onError(workbook.target.toCollection())
                ExportResult.FAILURE
            } finally {
                // Cleanup
                tempWrapperDir.deleteRecursively()
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun estimateExportSize(workbook: Workbook, chapterFilter: List<Int>): Long {
        val takes = gatherAudioFiles(workbook, chapterFilter)
        // Estimate audio size - assume MP3 conversion (default)
        val audioSize = takes.values.flatten().sumOf { audioFile ->
            when (AudioFileFormat.of(audioFile.extension)) {
                AudioFileFormat.MP3 -> audioFile.length()
                AudioFileFormat.WAV -> audioFile.length() / WAV_TO_MP3_COMPRESSED_RATE
                else -> audioFile.length()
            }
        }
        // Estimate text size (rough approximation)
        val sourceRCFile = workbook.source.resourceMetadata.path
        val rc = ResourceContainer.load(sourceRCFile)
        val textSize = rc.use {
            it.manifest.projects
                .filter { project -> project.path.contains(".usfm") }
                .sumOf { project ->
                    try {
                        it.accessor.getInputStream(project.path.removePrefix("./")).use { stream ->
                            stream.available().toLong()
                        }
                    } catch (e: Exception) {
                        0L
                    }
                }
        }
        return audioSize + textSize
    }

    private fun makeExportFilename(workbook: Workbook, metadata: ResourceMetadata): String {
        val lang = workbook.target.language.slug
        val resource = metadata.identifier
        val project = workbook.target.slug
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$resource-$project-$timestamp.orature"
    }

    private fun createTextBurritoMetadata(
        workbook: Workbook,
        rcInfo: Triple<List<org.wycliffeassociates.resourcecontainer.entity.Project>, org.wycliffeassociates.resourcecontainer.entity.DublinCore, LocalizedNamesSchema>,
        usfmFiles: List<Pair<String, File>>
    ): MetadataSchema {
        val language = workbook.target.language
        val langCode = language.slug
        val (projects, dublinCore, localizedNames) = rcInfo
        val defaultBookSlug = workbook.target.slug.uppercase(Locale.US)
        val bookSlugByPath = projects.associate { project ->
            project.path.removePrefix("./") to project.identifier.uppercase(Locale.US)
        }

        val ingredients = IngredientsSchema()
        usfmFiles.forEach { (path, file) ->
            val bookId = bookSlugByPath[path] ?: defaultBookSlug
            val ingredient = IngredientSchema().apply {
                this.mimeType = "text/usfm"
                this.size = file.length().toInt()
                this.checksum = Checksum().apply {
                    this.md5 = calculateMD5(file)
                }
                this.scope = ScopeSchema().apply {
                    put(bookId, mutableListOf())
                }
            }
            ingredients[path] = ingredient
        }

        return SourceMetadataSchema(
            Format.SCRIPTURE_BURRITO,
            SourceMetaSchema(
                dateCreated = Date.from(Instant.now()),
                version = MetaVersionSchema._1_0_0,
                defaultLocale = dublinCore.language.identifier,
                generator = SoftwareAndUserInfoSchema().apply {
                    softwareName = appName
                    softwareVersion = appVersion
                }
            ),
            idAuthorityProvider.createIdAuthority(),
            idAuthorityProvider.createIdentification().apply {
                this.name["en"] = "${workbook.target.resourceMetadata.title} (Text)"
                this.abbreviation["en"] = workbook.target.resourceMetadata.identifier
            },
            confidential = false,
            copyright = CopyrightSchema().apply {
                this.shortStatements =
                    mutableListOf(ShortStatement(dublinCore.rights, langCode))
            },
            type = TypeSchema(
                FlavorType(
                    name = Flavor.SCRIPTURE,
                    TextTranslationSchema(),
                    currentScope = ScopeSchema().apply {
                        usfmFiles.forEach { (path, _) ->
                            val bookId = bookSlugByPath[path] ?: defaultBookSlug
                            if (!containsKey(bookId)) {
                                put(bookId, mutableListOf())
                            }
                        }
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
            localizedNames = localizedNames,
            ingredients = ingredients
        )
    }

    private fun createAudioBurritoMetadata(
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        audioBurritoDir: File
    ): MetadataSchema {
        val language = workbook.target.language
        val langCode = language.slug
        val book = workbook.target.slug
        val mediaDir = File(audioBurritoDir, RcConstants.SOURCE_MEDIA_DIR)

        val ingredients = IngredientsSchema()

        takes.forEach { (chapterNumber, audioFiles) ->
            for (audioFile in audioFiles) {
                val fileName = audioFile.name
                val path = "${RcConstants.SOURCE_MEDIA_DIR}/$fileName"
                
                // Use the copied file in the burrito for checksum
                val burritoAudioFile = File(mediaDir, fileName)

                val ingredient = IngredientSchema().apply {
                    this.mimeType = when (audioFile.extension) {
                        AudioFileFormat.WAV.extension -> "audio/wav"
                        AudioFileFormat.MP3.extension -> "audio/mpeg"
                        AudioMetadataFileFormat.CUE.extension -> "application/x-cue"
                        else -> "application/octet-stream"
                    }
                    this.size = burritoAudioFile.length().toInt()
                    this.checksum = Checksum().apply {
                        this.md5 = calculateMD5(burritoAudioFile)
                    }
                    scope = ScopeSchema().apply {
                        put(
                            book.uppercase(Locale.US),
                            mutableListOf("$chapterNumber")
                        )
                    }
                }
                ingredients[path] = ingredient

                // Add timing file ingredient if it exists
                if (audioFile.extension in listOf(AudioFileFormat.WAV.extension, AudioFileFormat.MP3.extension)) {
                    val timingFileName = "${audioFile.nameWithoutExtension}.json"
                    val burritoTimingFile = File(mediaDir, timingFileName)
                    if (burritoTimingFile.exists()) {
                        val timingIngredient = IngredientSchema().apply {
                            this.mimeType = "application/json"
                            this.size = burritoTimingFile.length().toInt()
                            this.checksum = Checksum().apply {
                                this.md5 = calculateMD5(burritoTimingFile)
                            }
                            scope = ScopeSchema().apply {
                                put(
                                    book.uppercase(Locale.US),
                                    mutableListOf("$chapterNumber")
                                )
                            }
                            role = "timing"
                        }
                        val timingPath = "${RcConstants.SOURCE_MEDIA_DIR}/$timingFileName"
                        ingredients[timingPath] = timingIngredient
                    }
                }
            }
        }

        // Get source RC for copyright info
        val sourceRCFile = workbook.source.resourceMetadata.path
        val rc = ResourceContainer.load(sourceRCFile)

        return rc.use {
            SourceMetadataSchema(
                Format.SCRIPTURE_BURRITO,
                SourceMetaSchema(
                    dateCreated = Date.from(Instant.now()),
                    version = MetaVersionSchema._1_0_0,
                    defaultLocale = it.manifest.dublinCore.language.identifier,
                    generator = SoftwareAndUserInfoSchema().apply {
                        softwareName = appName
                        softwareVersion = appVersion
                    }
                ),
                idAuthorityProvider.createIdAuthority(),
                idAuthorityProvider.createIdentification().apply {
                    this.name["en"] = "${workbook.target.resourceMetadata.title} (Audio)"
                    this.abbreviation["en"] = workbook.target.resourceMetadata.identifier
                },
                confidential = false,
                copyright = CopyrightSchema().apply {
                    this.shortStatements =
                        mutableListOf(ShortStatement(it.manifest.dublinCore.rights, langCode))
                },
                type = TypeSchema(
                    FlavorType(
                        name = Flavor.SCRIPTURE,
                        AudioFlavorSchema(),
                        currentScope = ScopeSchema().apply {
                            this[book.uppercase(Locale.US)] =
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
                localizedNames = buildLocalizedNamesFromRC(it),
                ingredients = ingredients
            )
        }
    }

    private fun buildLocalizedNamesFromRC(rc: ResourceContainer): LocalizedNamesSchema {
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

    private fun createWrapperMetadata(workbook: Workbook): WrapperMetadata {
        val language = workbook.target.language
        val resource = workbook.target.resourceMetadata

        return WrapperMetadata(
            meta = WrapperMeta(
                name = hashMapOf("en" to resource.title),
                version = "0.0.1",
                generator = hashMapOf(
                    "name" to appName,
                    "version" to appVersion
                ),
                dateCreated = java.time.Instant.now().toString(),
                description = hashMapOf("en" to "Burrito wrapper containing text and audio burritos"),
                abbreviation = hashMapOf("en" to "${language.slug}_${resource.identifier} Burrito Wrapper")
            ),
            format = "scripture burrito wrapper",
            contents = WrapperContents(
                burritos = listOf(
                    WrapperBurrito(
                        id = "text",
                        path = "text",
                        role = "source"
                    ),
                    WrapperBurrito(
                        id = "audio",
                        path = "audio",
                        role = "derived"
                    )
                )
            )
        )
    }


    private fun writeBurritoMetadata(metadata: MetadataSchema, burritoDir: File) {
        val metadataFile = File(burritoDir, "metadata.json")
        mapper.writeValue(metadataFile, metadata)
    }

    private fun writeWrapperMetadata(metadata: WrapperMetadata, wrapperDir: File) {
        val metadataFile = File(wrapperDir, "metadata.json")
        mapper.writeValue(metadataFile, metadata)
    }

    private fun copyUsfmFilesToBurrito(
        usfmFiles: List<Pair<String, File>>,
        burritoDir: File
    ) {
        usfmFiles.forEach { (path, sourceFile) ->
            val targetFile = File(burritoDir, path)
            targetFile.parentFile.mkdirs()
            sourceFile.copyTo(targetFile, overwrite = true)
        }
    }

    private fun convertAudioFiles(
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        targetFormat: AudioFileFormat,
        callback: ProjectExporterCallback?
    ): Map<ChapterNumber, List<File>> {
        if (targetFormat == AudioFileFormat.WAV) {
            // No conversion needed, return original files
            return takes
        }

        val convertedTakes = mutableMapOf<ChapterNumber, List<File>>()
        val tempDir = File(directoryProvider.tempDirectory, "audio_conversion_${Date().time}")
        tempDir.mkdirs()

        try {
            takes.forEach { (chapterNumber, audioFiles) ->
                val convertedFiles = mutableListOf<File>()
                for (audioFile in audioFiles) {
                    val currentFormat = AudioFileFormat.of(audioFile.extension)
                    if (currentFormat == targetFormat) {
                        // Already in target format, use as-is
                        convertedFiles.add(audioFile)
                    } else if (currentFormat == AudioFileFormat.WAV && targetFormat == AudioFileFormat.MP3) {
                        // Convert WAV to MP3
                        val mp3File = File(tempDir, "${audioFile.nameWithoutExtension}.mp3")
                        val oratureAudio = OratureAudioFile(audioFile)
                        val cues = oratureAudio.getCues()
                        
                        val metadata = AudioExporter.ExportMetadata(
                            license = null,
                            contributors = listOf(),
                            markers = cues
                        )
                        
                        audioExporter.exportMp3(audioFile, mp3File, metadata).blockingAwait()
                        convertedFiles.add(mp3File)
                    } else {
                        // Unsupported conversion, use original
                        convertedFiles.add(audioFile)
                    }
                }
                convertedTakes[chapterNumber] = convertedFiles
            }
        } finally {
            // Note: We don't delete tempDir here as the files are still needed
            // They will be cleaned up when the wrapper temp directory is deleted
        }

        return convertedTakes
    }

    private fun copyAudioFilesToBurrito(
        workbook: Workbook,
        takes: Map<ChapterNumber, List<File>>,
        burritoDir: File,
        audioFormat: AudioFileFormat
    ) {
        val mediaDir = File(burritoDir, RcConstants.SOURCE_MEDIA_DIR)
        mediaDir.mkdirs()
        val book = workbook.target.slug

        takes.forEach { (chapterNumber, audioFiles) ->
            for (audioFile in audioFiles) {
                val targetFile = File(mediaDir, audioFile.name)
                audioFile.copyTo(targetFile, overwrite = true)

                // Create timing file for audio files
                if (audioFile.extension in listOf(AudioFileFormat.WAV.extension, AudioFileFormat.MP3.extension)) {
                    val oratureAudio = OratureAudioFile(audioFile)
                    val targetTimingFile = File(mediaDir, "${audioFile.nameWithoutExtension}.json")
                    BurritoAlignmentMetadata(targetTimingFile, audioFile)
                        .write(oratureAudio.getMarkers(), book, chapterNumber, oratureAudio.totalFrames)
                }
            }
        }
    }

    private fun gatherAudioFiles(
        workbook: Workbook,
        chapterFilter: List<Int>?
    ): Map<ChapterNumber, List<File>> {
        val takes = mutableMapOf<ChapterNumber, List<File>>()
        
        val chapters = workbook.target.chapters
            .filter { chapterFilter?.contains(it.sort) ?: true }
            .toList()
            .blockingGet()
        
        chapters.forEach { chapter ->
            val selectedTake = chapter.audio.selected.value?.value
            if (selectedTake != null && selectedTake.file.exists()) {
                takes[chapter.sort] = listOf(selectedTake.file)
            }
        }

        return takes
    }

    private fun createWrapperZip(wrapperDir: File, outputZip: File) {
        val zipAccessor = ZipAccessor(outputZip)
        val filesToWrite = wrapperDir.walkTopDown()
            .filter { it.isFile }
            .associate { file ->
                val relativePath = file.relativeTo(wrapperDir).path.replace("\\", "/")
                relativePath to { output: OutputStream ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                    Unit
                }
            }
        zipAccessor.use {
            it.write(filesToWrite)
        }
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

    // Wrapper metadata classes
    data class WrapperMetadata(
        val meta: WrapperMeta,
        val format: String,
        val contents: WrapperContents
    )

    data class WrapperMeta(
        val name: Map<String, String>,
        val version: String,
        val generator: Map<String, String>,
        val dateCreated: String,
        val description: Map<String, String>,
        val abbreviation: Map<String, String>
    )

    data class WrapperContents(
        val burritos: List<WrapperBurrito>
    )

    data class WrapperBurrito(
        val id: String,
        val path: String,
        val role: String
    )
}
