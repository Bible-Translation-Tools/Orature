package org.wycliffeassociates.otter.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import kotlin.io.path.createTempDirectory
import org.wycliffeassociates.resourcecontainer.entity.Language as RCLanguage

internal const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
internal const val CHAPTER_NARRATION_FILE_NAME = "chapter_narration.pcm"

class ResourceContainerBuilder(baseRC: File? = null) {

    private val tempDir = createTempDirectory("orature-test")
        .toFile()
        .apply { deleteOnExit() }

    private val selectedTakes: MutableList<String> = mutableListOf()
    private val takeCheckingMap = mutableMapOf<String, TakeCheckingState>()
    private val rcFile: File = baseRC ?: getDefaultRCFile()
    private val manifest: Manifest

    init {
        ResourceContainer.load(rcFile).use {
            this.manifest = it.manifest
        }
    }

    fun setVersion(version: Int): ResourceContainerBuilder {
        manifest.dublinCore.version = version.toString()
        return this
    }

    fun setTargetLanguage(language: Language): ResourceContainerBuilder {
        manifest.dublinCore.language = RCLanguage(
            identifier = language.slug,
            title = language.name,
            direction = language.direction
        )
        return this
    }

    fun setManifestSource(
        identifier: String,
        languageSlug: String,
        version: String
    ): ResourceContainerBuilder {
        manifest.dublinCore.source = mutableListOf(
            Source(identifier, languageSlug, version)
        )
        return this
    }

    fun setProjectManifest(
        title: String,
        identifier: String,
        sort: Int,
        path: String
    ): ResourceContainerBuilder {
        manifest.projects = listOf(
            Project(
                title = title,
                identifier = identifier,
                sort = sort,
                path = path
            )
        )
        return this
    }

    fun setProjectManifest(projects: List<Project>): ResourceContainerBuilder {
        manifest.projects = projects
        return this
    }

    fun setContributors(contributors: List<String>): ResourceContainerBuilder {
        manifest.dublinCore.contributor = contributors.toMutableList()
        return this
    }

    fun setOngoingProject(isOngoing: Boolean): ResourceContainerBuilder {
        if (isOngoing) {
            val pathInRC = RcConstants.SELECTED_TAKES_FILE
            val selectedFile = File(pathInRC).name
            val tempFile = tempDir.resolve(selectedFile).apply {
                createNewFile(); deleteOnExit()
            }

            ResourceContainer.load(rcFile).use {
                it.addFileToContainer(tempFile, pathInRC)
            }
        }
        return this
    }

    /**
     * Inserts a take to the current resource container.
     * If a take is selected, it will be added to the list of selected takes.
     *
     * For verse take, start and end must be provided
     */
    fun addTake(
        sort: Int,
        contentType: ContentType,
        takeNumber: Int,
        selected: Boolean,
        chapter: Int = sort,
        start: Int? = null,
        end: Int? = null,
        checking: TakeCheckingState? = null
    ): ResourceContainerBuilder {
        val fileName = FileNamer(
            start = start,
            end = end,
            sort = sort,
            contentType = contentType,
            languageSlug = manifest.dublinCore.language.identifier,
            bookSlug = manifest.projects.first().identifier,
            rcSlug = manifest.dublinCore.identifier,
            chunkCount = 90, // less than 100
            chapterCount = 90,
            chapterTitle = "",
            chapterSort = chapter
        ).generateName(takeNumber, AudioFileFormat.WAV)

        val tempTakeFile = createTestWavFile(tempDir)
        val takeToAdd = tempTakeFile.parentFile.resolve(fileName)
            .apply {
                parentFile.mkdirs()
                tempTakeFile.copyTo(this, overwrite = true)
                deleteOnExit()
            }

        val chapterDirTokens = "c${chapter.toString().padStart(2, '0')}"
        val relativePath = "$chapterDirTokens/$fileName"
        val pathInRC = if (selected && contentType == ContentType.META) {
            "${RcConstants.MEDIA_DIR}/$relativePath"
        } else {
            "${RcConstants.TAKE_DIR}/$relativePath"
        }
        ResourceContainer.load(rcFile).use {
            it.addFileToContainer(takeToAdd, pathInRC)
        }
        if (selected) {
            selectedTakes.add(relativePath)
        }
        if (checking != null) {
            takeCheckingMap[relativePath] = checking
        }

        return this
    }

    fun addInProgressNarration(sort: Int): ResourceContainerBuilder {
        val inProgressFiles = createTestChapterRepresentationFiles(tempDir)
        val chapterDirTokens = "c${sort.toString().padStart(2, '0')}"
        ResourceContainer.load(rcFile).use {
            inProgressFiles.forEach { file ->
                val relativePath = "$chapterDirTokens/${file.name}"
                val pathInRC = "${RcConstants.TAKE_DIR}/$relativePath"
                it.addFileToContainer(file, pathInRC)
            }
        }

        return this
    }

    fun build(): ResourceContainer {
        return ResourceContainer.load(rcFile).also { rc ->
            if (selectedTakes.isNotEmpty()) {
                rc.accessor.write(RcConstants.SELECTED_TAKES_FILE) { outputStream ->
                    outputStream.write(
                        selectedTakes.joinToString("\n").byteInputStream().readAllBytes()
                    )
                }
            }
            if (takeCheckingMap.isNotEmpty()) {
                writeCheckingStatusFile(rc)
            }

            rc.manifest = this.manifest
            rc.writeManifest()
        }
    }

    fun buildFile(): File {
        build().close()
        return rcFile
    }

    private fun getDefaultRCFile(): File {
        val sourcePath = javaClass.classLoader.getResource(
            "resource-containers/en_ulb.zip"
        ).file
        val sourceFile = File(sourcePath)
        val tempFile = tempDir.resolve("rc_test_generated.orature")
            .apply {
                deleteOnExit()
            }
        val rcMetadata = getResourceMetadata(getEnglishLanguage(1))

        ResourceContainer.create(tempFile) {
            manifest = Manifest(
                getDublinCore(rcMetadata),
                listOf(),
                Checking()
            )
            write()
        }.use { rc ->
            // add source
            rc.addFileToContainer(sourceFile, "${RcConstants.SOURCE_DIR}/${sourceFile.name}")
        }


        return tempFile
    }

    private fun writeCheckingStatusFile(rc: ResourceContainer) {
        rc.accessor.write(RcConstants.CHECKING_STATUS_FILE) { outputStream ->
            outputStream.use { stream ->
                val mapper = ObjectMapper(JsonFactory())
                    .registerKotlinModule()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)

                mapper.writeValue(stream, takeCheckingMap)
            }
        }
    }

    companion object {
        const val defaultProjectSlug = "jhn"
        const val checkingStatusFilePath = RcConstants.CHECKING_STATUS_FILE

        fun setUpEmptyProjectBuilder(): ResourceContainerBuilder {
            return ResourceContainerBuilder()
                .setManifestSource(
                    identifier = "ulb",
                    languageSlug = "en",
                    version = "12"
                )
                .setProjectManifest(
                    title = "John",
                    identifier = defaultProjectSlug,
                    sort = 1,
                    path = "./content"
                )
        }

        fun buildEmptyProjectFile(): File {
            return setUpEmptyProjectBuilder().buildFile()
        }
    }
}