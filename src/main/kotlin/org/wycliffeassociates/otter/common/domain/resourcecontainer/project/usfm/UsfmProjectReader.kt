package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm

import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.IOException

class UsfmProjectReader: IProjectReader {
    override fun constructProjectTree(
            container: ResourceContainer, project: Project
    ): Pair<ImportResult, Tree> {
        var result = ImportResult.SUCCESS
        val projectLocation = container.dir.resolve(project.path)
        val projectTree = Tree(project.toCollection())
        if (projectLocation.isDirectory) {
            val files = projectLocation.listFiles()
            for (file in files) {
                result = parseFileIntoProjectTree(file, projectTree, project.identifier)
                if (result != ImportResult.SUCCESS) return Pair(result, Tree(Unit))
            }
        } else {
            // Single file
            result = parseFileIntoProjectTree(projectLocation, projectTree, project.identifier)
            if (result != ImportResult.SUCCESS) return Pair(result, Tree(Unit))
        }
        return Pair(result, projectTree)
    }

    private fun parseFileIntoProjectTree(file: File, root: Tree, projectIdentifier: String): ImportResult {
        return when (file.extension) {
            "usfm", "USFM" -> {
                try {
                    val chapters = parseUSFMToChapterTrees(file, projectIdentifier)
                    root.addAll(chapters)
                    ImportResult.SUCCESS
                } catch (e: RuntimeException) {
                    ImportResult.INVALID_CONTENT
                }
            }
            else -> {
                ImportResult.UNSUPPORTED_CONTENT
            }
        }
    }

    private fun parseUSFMToChapterTrees(usfmFile: File, projectSlug: String): List<Tree> {
        if (usfmFile.extension != "usfm") {
            throw IOException("Not a USFM file")
        }

        val doc = ParseUsfm(usfmFile).parse()
        return doc.chapters.map { chapter ->
            val chapterSlug = "${projectSlug}_${chapter.key}"
            val chapterCollection = Collection(
                    chapter.key,
                    chapterSlug,
                    "chapter",
                    chapter.key.toString(),
                    null
            )
            val chapterTree = Tree(chapterCollection)
            // create a chunk for the whole chapter
            val chapChunk = Content(
                    0,
                    "chapter",
                    chapter.value.values.first().number,
                    chapter.value.values.last().number,
                    null,
                    null,
                    null
            )
            chapterTree.addChild(TreeNode(chapChunk))

            // Create content for each verse
            for (verse in chapter.value.values) {
                val content = Content(
                        verse.number,
                        "verse",
                        verse.number,
                        verse.number,
                        null,
                        null,
                        null
                )
                chapterTree.addChild(TreeNode(content))
            }
            return@map chapterTree
        }
    }
}