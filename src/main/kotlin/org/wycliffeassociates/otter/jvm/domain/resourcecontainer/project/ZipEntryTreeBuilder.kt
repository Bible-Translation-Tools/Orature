package org.wycliffeassociates.otter.jvm.domain.resourcecontainer.project

import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterZipFile.Companion.otterFileZ
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileSystem
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.util.zip.ZipFile
import java.util.ArrayDeque

object ZipEntryTreeBuilder : IZipEntryTreeBuilder {

    private fun createZipFileSystem(zipFilename: String): FileSystem {
        val path = Paths.get(zipFilename)
        return FileSystems.newFileSystem(path, null)
    }

    override fun buildOtterFileTree(zipFile: ZipFile, projectPath: String): OtterTree<OtterFile> {
        var treeRoot: OtterTree<OtterFile>? = null
        val treeCursor = ArrayDeque<OtterTree<OtterFile>>()
        createZipFileSystem(zipFile.name).use { zipFileSystem ->

            val projectRoot = zipFileSystem.getPath(projectPath)
            val sep = zipFileSystem.separator

            Files.walkFileTree(projectRoot, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(file: Path,
                                       attrs: BasicFileAttributes): FileVisitResult {
                    val filepath = file.toString().substringAfter(sep)
                    val entry = zipFile.getEntry(filepath)
                    val otterZipFile = otterFileZ(filepath, zipFile, sep, treeCursor.peek()?.value, entry)
                    treeCursor.peek()?.addChild(OtterTreeNode(otterZipFile))
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                    treeRoot = treeCursor.pop()
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path,
                                               attrs: BasicFileAttributes): FileVisitResult {
                    val newDirNode = OtterTree(
                            otterFileZ(dir.toString(), zipFile, zipFileSystem.separator, treeCursor.peek()?.value)
                    )
                    treeCursor.peek()?.addChild(newDirNode)
                    treeCursor.push(newDirNode)
                    return FileVisitResult.CONTINUE
                }
            })
            return treeRoot ?: OtterTree(otterFileZ(zipFile.name, zipFile, zipFileSystem.separator))
        }
    }
}

