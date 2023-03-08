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

    private val rcFile: File = baseRC ?: getDefaultRCFile()
    private val manifest: Manifest by lazy {
        ResourceContainer.load(rcFile).use {
            it.manifest
        }
    }
    private val selectedTakes: MutableList<String> = mutableListOf()

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

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

    fun addTake(
        takeNumber: Int,
        contentType: ContentType,
        selected: Boolean,
        sort: Int,
        chapter: Int,
        start: Int? = null,
        end: Int? = null
    ) {
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
                tempTakeFile.renameTo(this)
                deleteOnExit()
            }

        val chapterDirTokens = "c${chapter.toString().padStart(2, '0')}"
        val pathInRC = if (selected) {
            "content/$chapterDirTokens/$fileName"
        } else {
            ".apps/orature/takes/$chapterDirTokens/$fileName"
        }
        ResourceContainer.load(rcFile).use {
            it.addFileToContainer(takeToAdd, pathInRC)
        }
        selectedTakes.add("$chapterDirTokens/$fileName")
    }

    fun build(): ResourceContainer = ResourceContainer.load(rcFile).also { rc ->
        rc.manifest = this.manifest
        rc.writeManifest()

        if (selectedTakes.isNotEmpty()) {
            rc.accessor.write(".apps/orature/selected.txt") { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    selectedTakes.forEach {
                        writer.appendLine(it)
                    }
                }
            }
        }
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
            .apply { parentFile.deleteOnExit() }

        File(defaultPath).copyTo(tempFile)
        return tempFile
    }
}