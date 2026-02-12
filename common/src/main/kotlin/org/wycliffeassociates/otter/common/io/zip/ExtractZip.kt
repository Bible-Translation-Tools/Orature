package org.wycliffeassociates.otter.common.io.zip

import java.io.File
import java.util.zip.ZipFile
import kotlin.sequences.forEach

fun extractZip(zipFile: File, outputDir: File) {
    outputDir.mkdirs()
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            val outFile = File(outputDir, entry.name)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile.mkdirs()
                try {
                    zip.getInputStream(entry).use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}