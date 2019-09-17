package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm

import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.Reader

private const val FORMAT = "text/usfm"

class UsfmProjectReader : IProjectReader {
    private val currentDirectoryPrefix = Regex("""^\.?[/\\]""")

    /** @throws ImportException */
    override fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): OtterTree<CollectionOrContent> {
        // TODO 2/25/19
        return when (container.file.extension) {
            "zip" -> constructTreeFromZip(container, project)
            else -> constructTreeFromDirOrFile(container, project)
        }
    }

    private fun constructTreeFromDirOrFile(
        container: ResourceContainer,
        project: Project
    ): OtterTree<CollectionOrContent> {
        val projectTree = OtterTree<CollectionOrContent>(project.toCollection())

        val projectLocation = container.file.resolve(project.path)
        if (projectLocation.isDirectory) {
            projectLocation.listFiles()?.forEach { file ->
                val result = parseFileIntoProjectTree(file, projectTree, project.identifier)
                if (result != ImportResult.SUCCESS) throw ImportException(result)
            }
        } else {
            // Single file
            val result = parseFileIntoProjectTree(projectLocation, projectTree, project.identifier)
            if (result != ImportResult.SUCCESS) throw ImportException(result)
        }
        return projectTree
    }

    private fun constructTreeFromZip(
        container: ResourceContainer,
        project: Project
    ): OtterTree<CollectionOrContent> {
        if (!project.path.endsWith(".usfm", ignoreCase = true)) {
            throw ImportException(ImportResult.UNSUPPORTED_CONTENT)
        }
        return try {
            ResourceContainer.load(container.file).use { rc ->
                val projectPath = currentDirectoryPrefix.replace(project.path, "")
                rc.accessor.getReader(projectPath).use { reader ->
                    val projectTree = OtterTree<CollectionOrContent>(project.toCollection())
                    val result = parseFromReader(
                        reader,
                        projectTree,
                        project.identifier
                    )
                    if (result != ImportResult.SUCCESS) throw ImportException(result)
                    projectTree
                }
            }
        } catch (e: Exception) {
            throw e.castOrFindImportException() ?: ImportException(ImportResult.LOAD_RC_ERROR)
        }
    }
}

private fun parseFileIntoProjectTree(
    file: File,
    root: OtterTree<CollectionOrContent>,
    projectIdentifier: String
): ImportResult {
    return when (file.extension) {
        "usfm", "USFM" -> parseFromReader(file.bufferedReader(), root, projectIdentifier)
        else -> ImportResult.UNSUPPORTED_CONTENT
    }
}

private fun parseFromReader(
    reader: Reader,
    root: OtterTree<CollectionOrContent>,
    projectIdentifier: String
): ImportResult {
    return try {
        val chapters = parseUSFMToChapterTrees(reader, projectIdentifier)
        root.addAll(chapters)
        ImportResult.SUCCESS
    } catch (e: RuntimeException) {
        ImportResult.INVALID_CONTENT
    }
}

private fun parseUSFMToChapterTrees(reader: Reader, projectSlug: String): List<OtterTree<CollectionOrContent>> {
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
        val chapterTree = OtterTree<CollectionOrContent>(chapterCollection)
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
        chapterTree.addChild(OtterTreeNode(chapChunk))

        // Create content for each verse
        for (verse in chapter.value.values) {
            val content = Content(
                sort = verse.number,
                labelKey = ContentLabel.VERSE.value,
                start = verse.number,
                end = verse.number,
                selectedTake = null,
                text = verse.text,
                format = FORMAT,
                type = ContentType.TEXT
            )
            chapterTree.addChild(OtterTreeNode(content))
        }
        return@map chapterTree
    }
}
