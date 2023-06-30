package org.wycliffeassociates.otter.jvm.workbookapp.ui.system

/**
 * Opens the given filePath in the current OS file manager/explorer.
 */
fun openInFilesManager(filePath: String) {
    val osName = System.getProperty("os.name").uppercase()
    when {
        osName.contains("WIN") -> {
            val command = "explorer.exe /select,\"$filePath\""
            Runtime.getRuntime().exec(command)
        }
        osName.contains("MAC") -> {
            val command = arrayOf("open", "-R", filePath)
            Runtime.getRuntime().exec(command)
        }
        osName.contains("LINUX") -> {
            val command = arrayOf("xdg-open", filePath)
            Runtime.getRuntime().exec(command)
        }
    }
}