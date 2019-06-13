package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import org.wycliffeassociates.otter.common.collections.tree.OtterTree
import org.wycliffeassociates.otter.common.collections.tree.OtterTreeNode
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IProjectReader
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.OtterFile.Companion.otterFileF
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.zip.ZipFile

private const val FORMAT = "text/markdown"
private val extensions = Regex(".+\\.(md|mkdn?|mdown|markdown)$", RegexOption.IGNORE_CASE)

class MarkdownProjectReader() : IProjectReader {
    override fun constructProjectTree(
            container: ResourceContainer,
            project: Project,
            zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): Pair<ImportResult, Tree> {
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
                    ?.map<Any> { f -> contentList(f) ?: collection(collectionKey, f, projectRoot) }
                    ?.flattenContent()
                    ?.let { Pair(ImportResult.SUCCESS, it) }
                    ?: Pair(ImportResult.LOAD_RC_ERROR, Tree(Unit))
        } finally {
            toClose.forEach { it.close() }
        }
    }

    private fun fileToId(f: OtterFile): Int =
            f.nameWithoutExtension.toIntOrNull() ?: 0

    private fun fileToSlug(f: OtterFile, root: OtterFile): String =
            root.parentFile?.let { parentFile ->
                f.toRelativeString(parentFile)
                        .split('/', '\\')
                        .map { it.toIntOrNull()?.toString() ?: it }
                        .joinToString("_")
            } ?: throw Exception("fileToSlug() call should not be made with null parentFile") // TODO. Also we could move this exception somewhere else if we set parentFile to a val

    private fun bufferedReaderProvider(f: OtterFile): (() -> BufferedReader)? =
            if (f.isFile) {
                { f.bufferedReader() }
            } else null

    private fun collection(key: String, f: OtterFile, projectRoot: OtterFile): Collection =
            collection(key, fileToSlug(f, projectRoot), fileToId(f))

    private fun collection(key: String, slug: String, id: Int): Collection =
            Collection(
                    sort = id,
                    slug = slug,
                    labelKey = key,
                    titleKey = "$id",
                    resourceContainer = null)

    private fun content(sort: Int, id: Int, text: String, type: ContentType): Content? =
            if (text.isEmpty()) null
            else Content(sort, ContentLabel.of(type).value, id, id, null, text, FORMAT, type)

    private fun contentList(f: OtterFile): List<Content>? =
            bufferedReaderProvider(f)
                    ?.let { contentList(it, fileToId(f)) }

    private fun contentList(brp: () -> BufferedReader, fileId: Int): List<Content> {
        val helpResources = brp().use { ParseMd.parse(it) }
        var sort = 1
        return helpResources.flatMap { helpResource ->
            listOfNotNull(
                    content(sort++, fileId, helpResource.title, ContentType.TITLE),
                    content(sort++, fileId, helpResource.body, ContentType.BODY)
            )
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

private fun OtterTree<OtterFile>.filterMarkdownFiles(): OtterTree<OtterFile>? =
        this.filterPreserveParents { it.isFile && extensions.matches(it.name) }

private fun Tree.flattenContent(): Tree =
        Tree(this.value).also {
            it.addAll(
                    if (this.children.all { c -> c.value is List<*> }) {
                        this.children
                                .flatMap { c -> c.value as List<*> }
                                .filterNotNull()
                                .map { TreeNode(it) }
                    } else {
                        this.children
                                .map { if (it is Tree) it.flattenContent() else it }
                    }
            )
        }
