package org.wycliffeassociates.otter.common.persistence

import java.io.File

interface IDirectoryProvider {

    // create a directory to store the user's application projects/documents
    fun getUserDataDirectory(appendedPath: String = "") : File

    // create a directory to store the application's private org.wycliffeassociates.otter.common.data
    fun getAppDataDirectory(appendedPath: String = "") : File

    val userProfileImageDirectory: File
    val userProfileAudioDirectory: File
    val audioPluginDirectory: File
}