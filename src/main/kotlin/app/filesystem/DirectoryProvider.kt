package app.filesystem

import filesystem.IDirectoryProvider
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

class DirectoryProvider(private val appName: String) : IDirectoryProvider {

    private val separator = FileSystems.getDefault().separator   //if mac '/' if windows '\\'

    // private function to create a directory if it does not exist
    private fun makeDirectories(pathString: String): Boolean {
        var success: Boolean
        try {
            val path = Paths.get(pathString)
            if (Files.notExists(path)) {
                // path does not exist
                Files.createDirectories(path)
            }
            success = true
        } catch (e: Exception) {
            // could not create the path or the directories
            success = false
        }
        return success
    }

    // create a directory to store the user's application projects/documents
    override fun getUserDataDirectory(appendedPath: String, createIfNotExists: Boolean): String {
        // create the directory if it does not exist
        val pathComponents = mutableListOf(System.getProperty("user.home"), appName)
        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)
        val pathString = pathComponents.joinToString(separator)
        if (createIfNotExists && !makeDirectories(pathString)) {
            return ""
        }
        return pathString
    }

    // create a directory to store the application's private data
    override fun getAppDataDirectory(appendedPath: String, createIfNotExists: Boolean): String {
        // convert to upper case
        val os: String = System.getProperty("os.name")

        val upperOS = os.toUpperCase()

        val pathComponents = mutableListOf(appName)

        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)

        if (upperOS.contains("WIN")) {
            // on windows use app data
            pathComponents.add(0, System.getenv("APPDATA"))
        } else if (upperOS.contains("MAC")) {
            // use /Users/<user>/Library/Application Support/ for macOS
            pathComponents.add(0, "Application Support")
            pathComponents.add(0, "Library")
            pathComponents.add(0, System.getProperty("user.home"))
        } else if (upperOS.contains("LINUX")) {
            pathComponents.add(0, ".config")
            pathComponents.add(0, System.getProperty("user.home"))
        } else {
            // no path
            return ""
        }
        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(separator)
        if (createIfNotExists && !makeDirectories(pathString)) {
            return ""
        }

        return pathString
    }
}