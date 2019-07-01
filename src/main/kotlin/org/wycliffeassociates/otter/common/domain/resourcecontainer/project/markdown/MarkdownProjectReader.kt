package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile.Companion.otterFileF
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.Closeable
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.zip.ZipFile

private val extensions = Regex(".+\\.(md|mkdn?|mdown|markdown)$", RegexOption.IGNORE_CASE)

class MarkdownProjectReader(private val isHelp: Boolean) : IProjectReader {
    private val collectionForEachFile = !isHelp

    private data class Contents(val collection: Collection, val list: List<Content>?)

    /** @throws ImportException */
    override fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): OtterTree<CollectionOrContent> {
        val toClose = mutableListOf<Closeable>()
        try {
            val projectRoot: OtterFile
            val projectTreeRoot: OtterTree<OtterFile>

            when (container.file.extension) {
                "zip" -> {
                    val projectPathPrefixes = listOfNotNull(container.accessor.root, project.path)
                    projectRoot = projectPathPrefixes
                        .fold(container.file.toPath(), Path::resolve)
                        .normalize()
                        .toFile()
                        .let(::otterFileF)
                    val zip = ZipFile(container.file)
                    // The ZipEntry tree needs the ZipFile to stay open until later, so remember to close it.
                    toClose.add(zip)
                    projectTreeRoot = zipEntryTreeBuilder.buildOtterFileTree(zip, project.path, container.accessor.root)
                }
                else -> {
                    projectRoot = otterFileF(container.file.resolve(project.path))
                    projectTreeRoot = container.file.resolve(project.path).buildFileTree()
                }
            }

            val collectionKey = container.manifest.dublinCore.identifier

            return projectTreeRoot
                .filterMarkdownFiles()
                ?.map { f ->
                    Contents(
                        collection(collectionKey, f, projectRoot),
                        if (f.isFile) f.readContents() else null
                    )
                }
                ?.flattenContent()
                ?: throw ImportException(ImportResult.LOAD_RC_ERROR)
        } finally {
            toClose.forEach { it.close() }
        }
    }

    private fun fileToId(f: OtterFile): Int =
        f.nameWithoutExtension.toIntOrNull() ?: 0

    private fun fileToSlug(f: OtterFile, root: OtterFile): String =
        root.parentFile?.let { parentFile ->
            f.toRelativeString(parentFile)
                .substringBeforeLast('.')
                .split('/', '\\')
                .map { it.toIntOrNull()?.toString() ?: it }
                .joinToString("_")
        }
            ?: throw Exception("fileToSlug() call should not be made with null parentFile") // TODO. Also we could move this exception somewhere else if we set parentFile to a val

    private fun collection(key: String, f: OtterFile, projectRoot: OtterFile): Collection {
        val id = fileToId(f)
        return Collection(
            sort = id,
            slug = fileToSlug(f, projectRoot),
            labelKey = key,
            titleKey = "$id",
            resourceContainer = null
        )
    }

    private fun content(sort: Int, id: Int, text: String, type: ContentType): Content? =
        if (text.isEmpty()) null
        else Content(sort, ContentLabel.of(type).value, id, id, null, text, MimeType.MARKDOWN.norm, type)

    /**
     * Read an MD file and return its contents
     * @receiver must be a file, not a directory
     */
    private fun OtterFile.readContents(): List<Content> {
        val fileId = fileToId(this)
        var sort = 1
        return if (isHelp) {
            this.bufferedReader()
                .use { ParseMd.parseHelp(it) }
                .flatMap { helpResource ->
                    listOfNotNull(
                        content(sort++, fileId, helpResource.title, ContentType.TITLE),
                        content(sort++, fileId, helpResource.body, ContentType.BODY)
                    )
                }
        } else {
            this.bufferedReader()
                .use { ParseMd.parse(it) }
                .mapNotNull { content(sort++, fileId, it, ContentType.TEXT) }
        }
    }

    private fun OtterTree<OtterFile>.filterMarkdownFiles(): OtterTree<OtterFile>? =
        this.filterPreserveParents { it.isFile && extensions.matches(it.name) }

    /** Expand any list values to be individual tree nodes. */
    private fun OtterTree<Contents>.flattenContent(): OtterTree<CollectionOrContent> =
        OtterTree<CollectionOrContent>(this.value.collection).also { newRoot ->
            this.children.forEach { c ->
                if (c.value.list != null) {
                    // Add nodes from content list
                    val addTo =
                        if (collectionForEachFile)
                            OtterTree<CollectionOrContent>(c.value.collection).also(newRoot::addChild)
                        else
                            newRoot
                    addTo.addAll(c.value.list.map { OtterTreeNode(it) })
                } else if (c is OtterTree<Contents>) {
                    // Add node from child
                    newRoot.addChild(c.flattenContent())
                }
            }
        }
}

internal fun File.buildFileTree(): OtterTree<OtterFile> {
    var treeRoot: OtterTree<OtterFile>? = null
    val treeCursor = ArrayDeque<OtterTree<OtterFile>>()
    this.walkTopDown()
        .onEnter { newDir ->
            OtterTree(otterFileF(newDir)).let { newDirNode ->
                treeCursor.peek()?.addChild(newDirNode)
                treeCursor.push(newDirNode)
                true
            }
        }
        .onLeave { treeRoot = treeCursor.pop() }
        .filter { it.isFile }
        .map { OtterTreeNode(otterFileF(it)) }
        .forEach { treeCursor.peek()?.addChild(it) }
    return treeRoot ?: OtterTree(otterFileF(this))
}
