package org.wycliffeassociates.otter.common.utils

import java.io.File
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipUtils {
    companion object {
        /**
         * Unzip file to the target directory
         */
        fun unzip(zip: File, output: File? = null): File {
            output?.let { if (it.isFile) throw IllegalArgumentException("Output should be a directory") }

            val outputDir = output ?: createTempDir("orature_unzip")

            val zipFile = ZipFile(zip, Charset.defaultCharset())
            zipFile.entries().asSequence().forEach { entry ->
                zipFile.getInputStream(entry).use { input ->
                    val file = File(outputDir, entry.name)
                    file.parentFile.mkdirs()

                    if (!entry.isDirectory) {
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            return outputDir
        }

        /**
         * Zip directory
         */
        fun zip(input: File, zip: File? = null): File {
            if (input.isFile) throw IllegalArgumentException("Input should be a directory")
            zip?.let { if (it.isDirectory) throw IllegalArgumentException("Zip should be a file") }

            val zipFile = zip ?: createTempFile(input.name)

            val files = input.walkTopDown().filter { it.isFile }
            val entries = files.map { file ->
                val path = file.relativeTo(input).path
                val bytes = file.readBytes()
                path to bytes
            }

            zipFile.outputStream().use { fos ->
                ZipOutputStream(fos).use { zos ->
                    entries.forEach { entry ->
                        val (path, bytes) = entry
                        zos.putNextEntry(ZipEntry(path).apply { size = bytes.size.toLong() })
                        zos.write(bytes)
                        zos.closeEntry()
                    }
                }
            }
            return zipFile
        }

        private fun createTempFile(name: String): File {
            val tempDir = Files.createTempDirectory("orature_zip")
            val outPath = tempDir.resolve("$name.zip")
            val outFile = outPath.toFile()
            outFile.deleteOnExit()
            return outFile
        }
    }
}
