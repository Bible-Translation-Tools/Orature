package persistence

import java.io.File

interface IDirectoryProvider {

    // create a directory to store the user's application projects/documents
    fun getUserDataDirectory(appendedPath: String = "") : File

    // create a directory to store the application's private data
    fun getAppDataDirectory(appendedPath: String = "") : File

    val userProfileImageDirectory: File
    val userProfileAudioDirectory: File
}