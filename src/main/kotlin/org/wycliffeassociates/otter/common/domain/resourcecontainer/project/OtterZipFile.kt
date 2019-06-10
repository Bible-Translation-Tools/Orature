package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.io.BufferedReader
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class OtterZipFile(
        val absolutePath: String,
        private val rootZipFile: ZipFile,
        private val separator: String,
        val rootPathWithinZip: String?,
        val parentFile: OtterFile? = null,
        private val zipEntry: ZipEntry? = null
) {
    val isFile = !(zipEntry == null || zipEntry.isDirectory)
    val name: String = File(absolutePath).name
    val nameWithoutExtension = File(absolutePath).nameWithoutExtension

    fun bufferedReader(): BufferedReader = rootZipFile.getInputStream(zipEntry).bufferedReader()

    fun toRelativeString(parent: OtterFile): String {
        val suffixTrimmed = absolutePath.removeSuffix(separator)
        val prefixesToTrim = listOfNotNull(parent.absolutePath, rootPathWithinZip)
            .flatMap { listOf(it, ".$separator", separator) }
        return prefixesToTrim.fold(suffixTrimmed, String::removePrefix)
    }

    companion object {
        fun otterFileZ(
                absolutePath: String,
                rootZipFile: ZipFile,
                separator: String,
                rootPathWithinZip: String?,
                parentFile: OtterFile? = null
        ): OtterFile {
            val zipEntry = rootZipFile.getEntry(absolutePath)
            return OtterFile.Z(OtterZipFile(
                absolutePath = absolutePath,
                rootZipFile = rootZipFile,
                separator = separator,
                rootPathWithinZip = rootPathWithinZip,
                parentFile = parentFile,
                zipEntry = zipEntry
            ))
        }
    }
}
