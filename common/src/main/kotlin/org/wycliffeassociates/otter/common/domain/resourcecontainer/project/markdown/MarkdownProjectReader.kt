package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
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

private const val PRIMARY_COLLECTION_KEY = "project"
private const val SECONDARY_COLLECTION_KEY = "chapter"

class MarkdownProjectReader(private val isHelp: Boolean) : IProjectReader {
    private val collectionForEachFile = !isHelp

    private data class Contents(val collection: Collection, val list: List<Content>?)

    private data class ProjectFileTree(
        val tree: OtterTree<OtterFile>,
        val projectRoot: OtterFile,
        private val onClose: () -> Unit = {}
    ) : Closeable {
        override fun close() = onClose()
    }

    /** @throws ImportException */
    override fun constructProjectTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ): OtterTree<CollectionOrContent> {
        return buildProjectFileTree(container, project, zipEntryTreeBuilder)
            .use { (tree, projectRoot) ->
                tree
                    .filterMarkdownFiles()
                    .map { f ->
                        Contents(
                            collection(file = f, projectRoot = projectRoot, project = project),
                            if (f.isFile) f.readContents() else null
                        )
                    }
                    .apply {
                        // Set project info on the root node
                        value.collection.labelKey = PRIMARY_COLLECTION_KEY
                        value.collection.titleKey = project.title
                    }
                    .flattenContent()
                    .apply { addMetaContents() }
            }
    }

    private fun buildProjectFileTree(
        container: ResourceContainer,
        project: Project,
        zipEntryTreeBuilder: IZipEntryTreeBuilder
    ) = if (container.file.extension == "zip") {
        val projectPathPrefixes = listOfNotNull(container.accessor.root, project.path)
        val projectRoot = projectPathPrefixes
            .fold(container.file.toPath(), Path::resolve)
            .normalize()
            .toFile()
            .let(::otterFileF)

        val zip = ZipFile(container.file)
        val tree = zipEntryTreeBuilder.buildOtterFileTree(zip, project.path, container.accessor.root)
        ProjectFileTree(tree, projectRoot, zip::close)
    } else {
        val file = container.file.resolve(project.path)
        ProjectFileTree(file.buildFileTree(), otterFileF(file))
    }

    private fun fileToIndex(f: OtterFile): Int =
        f.nameWithoutExtension.toIntOrNull() ?: 0

    private fun fileToSlug(
        file: OtterFile,
        projectRoot: OtterFile,
        project: Project
    ): String {
        val fileParts = file
            .toRelativeString(projectRoot)
            .substringBeforeLast('.')
            .split('/', '\\')
            .asSequence()
        val withSlug = sequenceOf(project.identifier) + fileParts.drop(1)
        return withSlug.joinToString("_", transform = this::simplifyTitle)
    }

    private fun fileToSort(file: OtterFile) = when (file.nameWithoutExtension) {
        "back" -> 9999
        else -> fileToIndex(file)
    }

    private fun simplifyTitle(s: String) = s.toIntOrNull()?.toString() ?: s

    private fun collection(
        file: OtterFile,
        projectRoot: OtterFile,
        project: Project
    ) = Collection(
        sort = fileToSort(file),
        slug = fileToSlug(file = file, projectRoot = projectRoot, project = project),
        labelKey = SECONDARY_COLLECTION_KEY,
        titleKey = simplifyTitle(file.nameWithoutExtension),
        resourceContainer = null
    )

    private fun content(
        type: ContentType,
        index: Int,
        sort: Int? = null,
        text: String? = null,
        start: Int? = null,
        end: Int? = null
    ) = Content(
        sort = sort ?: index,
        labelKey = ContentLabel.of(type).value,
        start = start ?: index,
        end = end ?: index,
        selectedTake = null,
        text = text,
        format = MimeType.MARKDOWN.norm,
        type = type
    )

    /**
     * Read an MD file and return its contents
     * @receiver must be a file, not a directory
     */
    private fun OtterFile.readContents(): List<Content> {
        val fileId = fileToIndex(this)
        var sort = 1
        val contents = if (isHelp) {
            this.bufferedReader()
                .use { ParseMd.parseHelp(it) }
                .flatMap { helpResource ->
                    listOf(
                        content(ContentType.TITLE, fileId, sort++, helpResource.title),
                        content(ContentType.BODY, fileId, sort++, helpResource.body)
                    )
                }
        } else {
            this.bufferedReader()
                .use { ParseMd.parse(it) }
                .map {
                    val index = if (collectionForEachFile) sort else fileId
                    content(ContentType.TEXT, index, sort++, it)
                }
        }
        return contents.filterNot { it.text.isNullOrEmpty() }
    }

    private fun OtterTree<OtterFile>.filterMarkdownFiles(): OtterTree<OtterFile> =
        this.filterPreserveParents { it.isFile && extensions.matches(it.name) }
            ?: throw ImportException(ImportResult.LOAD_RC_ERROR) // No markdown found

    /** Expand any list values to be individual tree nodes. */
    private fun OtterTree<Contents>.flattenContent(): OtterTree<CollectionOrContent> =
        OtterTree<CollectionOrContent>(this.value.collection).also { newRoot ->
            this.children.forEach { c ->
                val child = c.value
                if (child.list != null) {
                    // Add nodes from content list
                    val addTo =
                        if (collectionForEachFile)
                            OtterTree<CollectionOrContent>(child.collection).also(newRoot::addChild)
                        else
                            newRoot
                    addTo.addAll(child.list.map { OtterTreeNode(it) })
                } else if (c is OtterTree<Contents>) {
                    // Add node from child
                    newRoot.addChild(c.flattenContent())
                }
            }
        }

    /** Modify this tree in-place to add META (chapter) contents for each chapter. */
    private fun OtterTree<CollectionOrContent>.addMetaContents() {
        if (isHelp) return
        this.children.forEach { (it as? OtterTree)?.addMetaContents() }
        val labelKey = (this.value as? Collection)?.labelKey
        if (labelKey == SECONDARY_COLLECTION_KEY) {
            addChild(chapterMetaNode(this))
        }
    }

    private fun chapterMetaNode(
        collection: OtterTree<CollectionOrContent>
    ): OtterTree<CollectionOrContent> {
        val contents = collection.children.mapNotNull { it.value as? Content }
        return OtterTree(
            content(
                type = ContentType.META,
                index = 0,
                start = contents.map { it.start }.min() ?: 0,
                end = contents.map { it.end }.max() ?: 0
            )
        )
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
