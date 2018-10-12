package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.model.Collection
import java.io.File

interface IDirectoryProvider {

    // Create a directory to store the user's application projects/documents
    fun getUserDataDirectory(appendedPath: String = "") : File

    // Create a directory to store the application's private org.wycliffeassociates.otter.common.data
    fun getAppDataDirectory(appendedPath: String = "") : File

    // Create the directory for a particular collection/content tree
    fun getProjectAudioDirectory(project: Collection, subcollections: List<Collection>): File

    val resourceContainerDirectory: File
    val userProfileImageDirectory: File
    val userProfileAudioDirectory: File
    val audioPluginDirectory: File
}