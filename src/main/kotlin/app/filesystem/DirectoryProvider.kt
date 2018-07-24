package app.filesystem

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

class DirectoryProvider(private val appName: String) {

    private val separator = FileSystems.getDefault().separator   //if mac '/' if windows '\\'

    // private function to create a directory if it does not exist
    private fun makeDirectories(pathString: String) : Boolean {
        var success : Boolean
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
    fun getUserDataDirectory(appendedPath: String = "", createIfNotExists: Boolean = true) : String {
        // create the directory if it does not exist
        var pathString = System.getProperty("user.home") + separator + appName
        if (appendedPath.isNotEmpty()) pathString += separator + appendedPath
        if (createIfNotExists && !makeDirectories(pathString)) {
            return ""
        }
        return pathString
    }

    // create a directory to store the application's private data
    fun getAppDataDirectory(appendedPath: String = "", createIfNotExists: Boolean = true) : String {
        // convert to upper case
        val os: String = System.getProperty("os.name")

        val upperOS = os.toUpperCase()

        var pathString = separator + appName
        if (appendedPath.isNotEmpty()) pathString += separator + appendedPath

        if (upperOS.contains("WIN")) {
            // on windows use app data
            pathString = System.getenv("APPDATA") + pathString
        } else if (upperOS.contains("MAC")) {
            // use /Users/<user>/Library/Application Support/ for macOS
            pathString = System.getProperty("user.home") +
                    "${separator}Library${separator}Application Support" +
                    pathString
        } else if (upperOS.contains("LINUX")) {
            // linux system use ~/.config/<app name>
            pathString = System.getProperty("user.home") + separator + ".config" + pathString
        } else {
            // no path
            return ""
        }
        // create the directory if it does not exist
        if (createIfNotExists && !makeDirectories(pathString)) {
            return ""
        }

        return pathString
    }
}