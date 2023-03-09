package integrationtest

import integrationtest.di.DaggerTestPersistenceComponent
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import javax.inject.Inject
import org.wycliffeassociates.resourcecontainer.entity.Language as RCLanguage

class ResourceContainerBuilder(baseRC: File? = null) {

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val manifest: Manifest by lazy {
        ResourceContainer.load(rcFile).use {
            it.manifest
        }
    }
    private val selectedTakes: MutableList<String> = mutableListOf()
    private val rcFile: File = baseRC ?: getDefaultRCFile()

    fun setTargetLanguage(language: Language): ResourceContainerBuilder {
        manifest.dublinCore.language = RCLanguage(
            identifier = language.slug,
            title = language.name,
            direction = language.direction
        )
        return this
    }

    fun setProjectManifest(
        title: String,
        identifier: String,
        sort: Int,
        path: String
    ): ResourceContainerBuilder {
        TODO()
    }

    fun setProjectManifest(projects: List<Project>) {
        TODO()
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

        val tempTakeFile = createTestWavFile(directoryProvider.tempDirectory)
        val takeToAdd = tempTakeFile.parentFile.resolve(fileName)
            .apply {
                parentFile.mkdirs()
                tempTakeFile.copyTo(this, overwrite = true)
                deleteOnExit()
            }

        val chapterDirTokens = "c${chapter.toString().padStart(2, '0')}"
        val pathInRC = if (selected && contentType == ContentType.META) {
            "content/$chapterDirTokens/$fileName"
        } else {
            ".apps/orature/takes/$chapterDirTokens/$fileName"
        }
        ResourceContainer.load(rcFile).use {
            it.addFileToContainer(takeToAdd, pathInRC)
        }
        selectedTakes.add("$chapterDirTokens/$fileName")

        return this
    }

    fun build(): ResourceContainer = ResourceContainer.load(rcFile).also { rc ->
        if (selectedTakes.isNotEmpty()) {
            rc.accessor.write(".apps/orature/selected.txt") { outputStream ->
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
        val defaultPath = javaClass.classLoader.getResource(
            "resource-containers/ade-jhn-base-project.orature"
        ).file

        val tempFile = directoryProvider.createTempFile("rc_test_generated", ".orature")
            .apply { deleteOnExit() }

        File(defaultPath).copyTo(tempFile, overwrite = true)
        return tempFile
    }
}