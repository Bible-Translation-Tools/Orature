package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.io.zip.IZipFileReader
import org.wycliffeassociates.otter.common.io.zip.IZipFileWriter
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File

interface IDirectoryProvider {

    /** Directory to store the user's application projects/documents */
    fun getUserDataDirectory(appendedPath: String = ""): File

    /** Directory to store the application's private data */
    fun getAppDataDirectory(appendedPath: String = ""): File

    /** Directory for project audio */
    fun getProjectAudioDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        book: Collection
    ): File

    /** Directory for project audio */
    fun getProjectAudioDirectory(
        source: ResourceMetadata,
        target: ResourceMetadata?,
        bookSlug: String
    ): File

    /** Internal-use directory of the given source RC */
    fun getSourceContainerDirectory(container: ResourceContainer): File

    /** Internal-use directory of the given source RC */
    fun getSourceContainerDirectory(metadata: ResourceMetadata): File

    /** Internal-use directory of the given derived RC */
    fun getDerivedContainerDirectory(metadata: ResourceMetadata, source: ResourceMetadata): File

    /** Create a new IZipFileReader */
    fun newZipFileReader(zip: File): IZipFileReader

    /** Create a new IZipFileWriter */
    fun newZipFileWriter(zip: File): IZipFileWriter

    val resourceContainerDirectory: File
    val userProfileImageDirectory: File
    val userProfileAudioDirectory: File
    val audioPluginDirectory: File
    val logsDirectory: File
}