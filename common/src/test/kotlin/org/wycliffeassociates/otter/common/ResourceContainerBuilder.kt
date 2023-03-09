package org.wycliffeassociates.otter.common

import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.RcConstants
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import kotlin.io.path.createTempDirectory
import org.wycliffeassociates.resourcecontainer.entity.Language as RCLanguage

class ResourceContainerBuilder(baseRC: File? = null) {

    private val tempDir = createTempDirectory("orature-test").toFile()
        .apply { deleteOnExit() }

    private val selectedTakes: MutableList<String> = mutableListOf()
    private val rcFile: File = baseRC ?: getDefaultRCFile()
    private val manifest: Manifest

    init {
        ResourceContainer.load(rcFile).use {
            this.manifest = it.manifest
        }
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

    fun setProjectManifest(projects: List<Project>) {
        manifest.projects = projects
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
        end: Int? = null
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
        val pathInRC = if (selected && contentType == ContentType.META) {
            "${RcConstants.MEDIA_DIR}/$chapterDirTokens/$fileName"
        } else {
            "${RcConstants.TAKE_DIR}/$chapterDirTokens/$fileName"
        }
        ResourceContainer.load(rcFile).use {
            it.addFileToContainer(takeToAdd, pathInRC)
        }
        selectedTakes.add("$chapterDirTokens/$fileName")

        return this
    }

    fun build(): ResourceContainer = ResourceContainer.load(rcFile).also { rc ->
        if (selectedTakes.isNotEmpty()) {
            rc.accessor.write(RcConstants.SELECTED_TAKES_FILE) { outputStream ->
                outputStream.write(
                    selectedTakes.joinToString("\n").byteInputStream().readAllBytes()
                )
            }
        }

        rc.manifest = this.manifest
        rc.writeManifest()
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

    companion object {
        fun setUpEmptyProjectBuilder(): ResourceContainerBuilder {
            return ResourceContainerBuilder()
                .setManifestSource(
                    identifier = "ulb",
                    languageSlug = "en",
                    version = "12"
                )
                .setProjectManifest(
                    title = "John",
                    identifier = "jhn",
                    sort = 1,
                    path = "./content"
                )
        }

        fun buildEmptyProjectFile(): File {
            return setUpEmptyProjectBuilder().buildFile()
        }
    }
}