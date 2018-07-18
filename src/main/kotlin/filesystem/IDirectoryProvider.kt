package filesystem

interface IDirectoryProvider {

    // create a directory to store the user's application projects/documents
    fun getUserDataDirectory(appendedPath: String = "", createIfNotExists: Boolean = true) : String

    // create a directory to store the application's private data
    fun getAppDataDirectory(appendedPath: String = "", createIfNotExists: Boolean = true) : String
}