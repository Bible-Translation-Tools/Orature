package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.io.file.NioDirectoryFileReader
import org.wycliffeassociates.otter.jvm.workbookapp.io.zip.NioZipFileReader
import org.wycliffeassociates.otter.jvm.workbookapp.io.zip.NioZipFileWriter
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.FileSystems

class DirectoryProvider(
    private val appName: String,
    pathSeparator: String? = null,
    userHome: String? = null,
    windowsAppData: String? = null,
    osName: String? = null
) : IDirectoryProvider {
    private val pathSeparator = pathSeparator ?: FileSystems.getDefault().separator
    private val userHome = userHome ?: System.getProperty("user.home")
    private val windowsAppData = windowsAppData ?: System.getenv("APPDATA")
    private val osName = (osName ?: System.getProperty("os.name")).toUpperCase()

    // create a directory to store the user's application projects/documents
    override fun getUserDataDirectory(appendedPath: String): File {
        // create the directory if it does not exist
        val pathComponents = mutableListOf(userHome, appName)
        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)
        val pathString = pathComponents.joinToString(pathSeparator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    // create a directory to store the application's private data
    override fun getAppDataDirectory(appendedPath: String): File {
        val pathComponents = mutableListOf<String>()

        when {
            osName.contains("WIN") -> pathComponents.add(windowsAppData)
            osName.contains("MAC") -> {
                // use /Users/<user>/Library/Application Support/ for macOS
                pathComponents.add(userHome)
                pathComponents.add("Library")
                pathComponents.add("Application Support")
            }
            osName.contains("LINUX") -> {
                pathComponents.add(userHome)
                pathComponents.add(".config")
            }
        }

        pathComponents.add(appName)

        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)

        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(pathSeparator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    override fun getProjectDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        book: Collection
    ) = getProjectDirectory(source, target, book.slug)

    override fun getProjectDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        bookSlug: String
    ): File {
        // Audio is being stored in the source creator directory for resources
        val targetCreator = when {
            target?.type == ContainerType.Help -> source.creator
            target?.creator != null -> target.creator
            else -> "."
        }
        val appendedPath = listOf(
            targetCreator,
            source.creator,
            "${source.language.slug}_${source.identifier}",
            "v${target?.version ?: "-none"}",
            target?.language?.slug ?: "no_language",
            bookSlug
        ).joinToString(pathSeparator)
        val path = getUserDataDirectory(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getProjectAudioDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        book: Collection
    ) = getProjectAudioDirectory(source, target, book.slug)

    override fun getProjectAudioDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        bookSlug: String
    ): File {
        val path = getProjectDirectory(source, target, bookSlug)
            .resolve(".apps")
            .resolve("orature")
            .resolve("takes")
        path.mkdirs()
        return path
    }

    override fun getProjectSourceDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        book: Collection
    ) = getProjectSourceDirectory(source, target, book.slug)

    override fun getProjectSourceDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        bookSlug: String
    ): File {
        val path = getProjectDirectory(source, target, bookSlug)
            .resolve(".apps")
            .resolve("orature")
            .resolve("source")
        path.mkdirs()
        return path
    }

    override fun getSourceContainerDirectory(container: ResourceContainer): File {
        val dublinCore = container.manifest.dublinCore
        container.close()
        val appendedPath = listOf(
            "src",
            dublinCore.creator,
            "${dublinCore.language.identifier}_${dublinCore.identifier}",
            "v${dublinCore.version}"
        ).joinToString(pathSeparator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getSourceContainerDirectory(metadata: ResourceMetadata): File {
        return listOf(
            "src",
            metadata.creator,
            "${metadata.language.slug}_${metadata.identifier}",
            "v${metadata.version}"
        )
            .fold(resourceContainerDirectory, File::resolve)
            .apply { mkdirs() }
    }

    override fun getDerivedContainerDirectory(metadata: ResourceMetadata, source: ResourceMetadata): File {
        val appendedPath = listOf(
            "der",
            metadata.creator,
            source.creator,
            "${source.language.slug}_${source.identifier}",
            "v${metadata.version}",
            metadata.language.slug
        ).joinToString(pathSeparator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override fun newFileWriter(file: File) = NioZipFileWriter(file)

    override fun newFileReader(file: File): IFileReader {
        return when {
            file.isDirectory -> NioDirectoryFileReader(file)
            file.isFile && file.extension == "zip" -> NioZipFileReader(file)
            else -> throw IllegalArgumentException("File type not supported")
        }
    }

    override val resourceContainerDirectory: File
        get() = getAppDataDirectory("rc")

    override val userProfileAudioDirectory: File
        get() = getAppDataDirectory("users${pathSeparator}audio")

    override val userProfileImageDirectory: File
        get() = getAppDataDirectory("users${pathSeparator}images")

    override val audioPluginDirectory: File
        get() = getAppDataDirectory("plugins")

    override val logsDirectory: File
        get() = getAppDataDirectory("logs")

    override val cacheDirectory: File
        get() = getAppDataDirectory("cache")
}
