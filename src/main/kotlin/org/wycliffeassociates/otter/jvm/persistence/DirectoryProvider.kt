package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.nio.file.FileSystems

class DirectoryProvider(private val appName: String) : IDirectoryProvider {

    private val separator = FileSystems.getDefault().separator   //if mac '/' if windows '\\'

    // create a directory to store the user's application projects/documents
    override fun getUserDataDirectory(appendedPath: String): File {
        // create the directory if it does not exist
        val pathComponents = mutableListOf(System.getProperty("user.home"), appName)
        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)
        val pathString = pathComponents.joinToString(separator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    // create a directory to store the application's private data
    override fun getAppDataDirectory(appendedPath: String): File {
        // convert to upper case
        val os: String = System.getProperty("os.name")

        val upperOS = os.toUpperCase()

        val pathComponents = mutableListOf<String>()

        when {
            upperOS.contains("WIN") -> pathComponents.add(System.getenv("APPDATA"))
            upperOS.contains("MAC") -> {
                // use /Users/<user>/Library/Application Support/ for macOS
                pathComponents.add(System.getProperty("user.home"))
                pathComponents.add("Library")
                pathComponents.add("Application Support")
            }
            upperOS.contains("LINUX") -> {
                pathComponents.add(System.getProperty("user.home"))
                pathComponents.add(".config")
            }
        }

        pathComponents.add(appName)

        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)

        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(separator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    override fun getProjectAudioDirectory(
            sourceMetadata: ResourceMetadata,
            book: Collection,
            chapterDirName: String
    ): File {
        val appendedPath = listOf(
                book.resourceContainer?.creator ?: ".",
                sourceMetadata.creator,
                "${sourceMetadata.language.slug}_${sourceMetadata.identifier}",
                "v${book.resourceContainer?.version ?: "-none"}",
                book.resourceContainer?.language?.slug ?: "no_language",
                book.slug,
                chapterDirName
        ).joinToString(separator)
        val path = getUserDataDirectory(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getSourceContainerDirectory(container: ResourceContainer): File {
        val dublinCore = container.manifest.dublinCore
        val appendedPath = listOf(
                "src",
                dublinCore.creator,
                "${dublinCore.language.identifier}_${dublinCore.identifier}",
                "v${dublinCore.version}"
        ).joinToString(separator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getDerivedContainerDirectory(metadata: ResourceMetadata, source: ResourceMetadata): File {
        val appendedPath = listOf(
                "der",
                metadata.creator,
                source.creator,
                "${source.language.slug}_${source.identifier}",
                "v${metadata.version}",
                metadata.language.slug
        ).joinToString(separator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override val resourceContainerDirectory: File
        get() = getAppDataDirectory("rc")

    override val userProfileAudioDirectory: File
        get() = getAppDataDirectory("users${separator}audio")

    override val userProfileImageDirectory: File
        get() = getAppDataDirectory("users${separator}images")

    override val audioPluginDirectory: File
        get() = getAppDataDirectory("plugins")

}