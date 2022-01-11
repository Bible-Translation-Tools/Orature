/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.project

import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterZipFile.Companion.otterFileZ
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.io.path.absolutePathString

class ZipEntryTreeBuilder @Inject constructor() : IZipEntryTreeBuilder {

    private fun createZipFileSystem(zipFilename: String): FileSystem {
        val path = Paths.get(zipFilename)
        return FileSystems.newFileSystem(path, null)
    }

    override fun buildOtterFileTree(
        zipFile: ZipFile,
        projectPath: String,
        rootPathWithinZip: String?
    ): OtterTree<OtterFile> {
        var treeRoot: OtterTree<OtterFile>? = null
        val treeCursor = ArrayDeque<OtterTree<OtterFile>>()
        createZipFileSystem(zipFile.name).use { zipFileSystem ->

            val projectRoot = zipFileSystem.getPath(rootPathWithinZip ?: "", projectPath).normalize()
            val sep = zipFileSystem.separator

            Files.walkFileTree(
                projectRoot,
                object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(
                        file: Path,
                        attrs: BasicFileAttributes
                    ): FileVisitResult {
                        // Previously used file.toString, but the path was not absolute
                        val filepath = file.absolutePathString().substringAfter(sep)
                        val otterZipFile =
                            otterFileZ(filepath, zipFile, sep, rootPathWithinZip, treeCursor.peek()?.value)
                        treeCursor.peek()?.addChild(OtterTreeNode(otterZipFile))
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                        treeRoot = treeCursor.pop()
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun preVisitDirectory(
                        dir: Path,
                        attrs: BasicFileAttributes
                    ): FileVisitResult {
                        val newDirNode = OtterTree(
                            otterFileZ(
                                absolutePath = dir.absolutePathString().substringAfter(sep),
                                rootZipFile = zipFile,
                                separator = zipFileSystem.separator,
                                rootPathWithinZip = rootPathWithinZip,
                                parentFile = treeCursor.peek()?.value
                            )
                        )
                        treeCursor.peek()?.addChild(newDirNode)
                        treeCursor.push(newDirNode)
                        return FileVisitResult.CONTINUE
                    }
                }
            )
            return treeRoot ?: OtterTree(
                otterFileZ(
                    absolutePath = zipFile.name,
                    rootZipFile = zipFile,
                    separator = zipFileSystem.separator,
                    rootPathWithinZip = rootPathWithinZip
                )
            )
        }
    }
}
