package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.io.BufferedReader
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class OtterZipFile(
        val absolutePath: String,
        private val rootZipFile: ZipFile,
        private val separator: String,
        val parentFile: OtterFile? = null,
        private val zipEntry: ZipEntry? = null
) {
    val isFile = zipEntry != null
    val name: String = File(absolutePath).name
    val nameWithoutExtension = File(absolutePath).nameWithoutExtension

    fun bufferedReader(): BufferedReader = rootZipFile.getInputStream(zipEntry).bufferedReader()

    fun toRelativeString(parent: OtterFile): String = absolutePath
            .substringAfter(parent.absolutePath)
            .removePrefix(".")
            .removePrefix(separator)
            .removeSuffix(separator)

    companion object {
        fun otterFileZ(
                absolutePath: String,
                rootZipFile: ZipFile,
                separator: String,
                parentFile: OtterFile? = null,
                zipEntry: ZipEntry? = null
        ): OtterFile {
            return OtterFile.Z(OtterZipFile(absolutePath, rootZipFile, separator, parentFile, zipEntry))
        }
    }
}

