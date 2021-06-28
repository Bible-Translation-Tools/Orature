package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm

import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.data.primitives.CollectionOrContent
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.castOrFindImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.usfmtools.USFMParser
import org.wycliffeassociates.usfmtools.models.markers.CMarker
import org.wycliffeassociates.usfmtools.models.markers.TextBlock
import org.wycliffeassociates.usfmtools.models.markers.VMarker
import java.io.File
import java.io.Reader
import org.wycliffeassociates.usfmtools.models.markers.FMarker
import org.wycliffeassociates.usfmtools.models.markers.Marker

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
    val usfmText = reader.readText()
    val parser = USFMParser(arrayListOf("s5"))
    val doc = parser.parseFromString(usfmText)
    val chapters = doc.getChildMarkers(CMarker::class.java)
    return chapters.map { chapter ->
        val verses = chapter.getChildMarkers(VMarker::class.java)
        val startVerse = verses.maxByOrNull { it.startingVerse }?.startingVerse ?: 1
        val endVerse = verses.maxByOrNull { it.endingVerse }?.endingVerse ?: 1
        val chapterSlug = "${projectSlug}_${chapter.number}"
        val chapterCollection = Collection(
            sort = chapter.number,
            slug = chapterSlug,
            labelKey = ContentLabel.CHAPTER.value,
            titleKey = chapter.number.toString(),
            resourceContainer = null
        )
        val chapterTree = OtterTree<CollectionOrContent>(chapterCollection)
        // create a chunk for the whole chapter
        val chapChunk = Content(
            sort = 0,
            labelKey = ContentLabel.CHAPTER.value,
            start = startVerse,
            end = endVerse,
            selectedTake = null,
            text = null,
            format = null,
            type = ContentType.META
        )
        chapterTree.addChild(OtterTreeNode(chapChunk))

        // Create content for each verse
        for (verse in verses) {
            val content = Content(
                sort = verse.startingVerse,
                labelKey = ContentLabel.VERSE.value,
                start = verse.startingVerse,
                end = verse.endingVerse,
                selectedTake = null,
                text = verse.getText(),
                format = FORMAT,
                type = ContentType.TEXT
            )
            chapterTree.addChild(OtterTreeNode(content))
        }
        return@map chapterTree
    }
}

fun VMarker.getText(): String {
    val sb = StringBuilder()
    val text = getTextBlocks()
    for (txt in text) {
        sb.append(txt.text)
    }
    return sb.toString()
}

fun Marker.getTextBlocks(): MutableList<TextBlock> {
    val textBlocks = mutableListOf<TextBlock>()
    for (marker in contents) {
        when (marker) {
            is FMarker -> continue
            is TextBlock -> textBlocks.add(marker)
            else -> {
                marker
                    .getChildMarkers(TextBlock::class.java)
                    .forEach { textBlocks.addAll(it.getTextBlocks()) }
            }
        }
    }
    return textBlocks
}
