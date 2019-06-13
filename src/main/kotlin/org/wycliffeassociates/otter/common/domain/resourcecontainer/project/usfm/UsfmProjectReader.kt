package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm

import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.Reader

class UsfmProjectReader : IProjectReader {
    private val currentDirectoryPrefix = Regex("""^\.?[/\\]""")

    override fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): Pair<ImportResult, Tree> {
        // TODO 2/25/19
        return when (container.file.extension) {
            "zip" -> constructTreeFromZip(container, project)
            else -> constructTreeFromDirOrFile(container, project)
        }
    }

    private fun constructTreeFromDirOrFile(
        container: ResourceContainer,
        project: Project
    ): Pair<ImportResult, Tree> {
        var result: ImportResult = ImportResult.SUCCESS
        val projectTree = Tree(project.toCollection())

        val projectLocation = container.file.resolve(project.path)
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

    private fun constructTreeFromZip(
        container: ResourceContainer,
        project: Project
    ): Pair<ImportResult, Tree> {
        return if (!project.path.endsWith(".usfm", ignoreCase = true)) {
            Pair(ImportResult.UNSUPPORTED_CONTENT, Tree(Unit))
        } else try {
            ResourceContainer.load(container.file).use { rc ->
                val projectPath = currentDirectoryPrefix.replace(project.path, "")
                rc.accessor.getReader(projectPath).use { reader ->
                    val projectTree = Tree(project.toCollection())
                    val result = parseFromReader(
                        reader,
                        projectTree,
                        project.identifier
                    )
                    Pair(
                        result,
                        if (result == ImportResult.SUCCESS) projectTree else Tree(Unit)
                    )
                }
            }
        } catch (e: Exception) {
            Pair(e.castOrFindImportException()?.result ?: ImportResult.LOAD_RC_ERROR, Tree(Unit))
        }
    }
}

private fun parseFileIntoProjectTree(
    file: File,
    root: Tree,
    projectIdentifier: String
): ImportResult {
    return when (file.extension) {
        "usfm", "USFM" -> parseFromReader(file.bufferedReader(), root, projectIdentifier)
        else -> ImportResult.UNSUPPORTED_CONTENT
    }
}

private fun parseFromReader(reader: Reader, root: Tree, projectIdentifier: String): ImportResult {
    return try {
        val chapters = parseUSFMToChapterTrees(reader, projectIdentifier)
        root.addAll(chapters)
        ImportResult.SUCCESS
    } catch (e: RuntimeException) {
        ImportResult.INVALID_CONTENT
    }
}

private fun parseUSFMToChapterTrees(reader: Reader, projectSlug: String): List<Tree> {
    val doc = ParseUsfm(reader).parse()
    return doc.chapters.map { chapter ->
        val chapterSlug = "${projectSlug}_${chapter.key}"
        val chapterCollection = Collection(
            sort = chapter.key,
            slug = chapterSlug,
            labelKey = ContentLabel.CHAPTER.value,
            titleKey = chapter.key.toString(),
            resourceContainer = null
        )
        val chapterTree = Tree(chapterCollection)
        // create a chunk for the whole chapter
        val chapChunk = Content(
            sort = 0,
            labelKey = ContentLabel.CHAPTER.value,
            start = chapter.value.values.first().number,
            end = chapter.value.values.last().number,
            selectedTake = null,
            text = null,
            format = null,
            type = ContentType.META
        )
        chapterTree.addChild(TreeNode(chapChunk))

        // Create content for each verse
        for (verse in chapter.value.values) {
            val content = Content(
                sort = verse.number,
                labelKey = ContentLabel.VERSE.value,
                start = verse.number,
                end = verse.number,
                selectedTake = null,
                text = null,
                format = null,
                type = ContentType.TEXT
            )
            chapterTree.addChild(TreeNode(content))
        }
        return@map chapterTree
    }
}
