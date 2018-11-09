package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.usfm.ParseUsfm
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IChunkRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.errors.RCException
import java.io.File
import java.io.IOException


class ImportResourceContainer(
        private val languageRepository: ILanguageRepository,
        private val metadataRepository: IResourceMetadataRepository,
        private val collectionRepository: ICollectionRepository,
        private val chunkRepository: IChunkRepository,
        directoryProvider: IDirectoryProvider
) {

    private val rcDirectory = File(directoryProvider.getAppDataDirectory(), "rc")

    //TODO: Remove this when Bible, OT, NT are included as part of a resource container
    fun importBible(meta: ResourceMetadata) {
        //Initialize bible and testament collections
        val bible = Collection(1, "bible", "bible", "Bible", meta)
        val ot = Collection(1, "bible-ot", "testament", "Old Testament", meta)
        val nt = Collection(2, "bible-nt", "testament", "New Testament", meta)
        val bibleid = collectionRepository.insert(bible).blockingGet()
        bible.id = bibleid
        val otid = collectionRepository.insert(ot).blockingGet()
        ot.id = otid
        val ntid = collectionRepository.insert(nt).blockingGet()
        nt.id = ntid
        collectionRepository.updateParent(ot, bible).subscribe()
        collectionRepository.updateParent(nt, bible).subscribe()
    }

    fun import(file: File): Completable {
        return when {
            file.isDirectory -> importDirectory(file)
            else -> Completable.complete()
        }
    }

    private fun importDirectory(dir: File): Completable {
        if (validateResourceContainer(dir)) {
            if (dir.parentFile?.absolutePath != rcDirectory.absolutePath) {
                val success = dir.copyRecursively(File(rcDirectory, dir.name), true)
                if (!success) {
                    throw IOException("Could not copy resource container ${dir.name} to resource container directory")
                }
            }
            return importResourceContainer(File(rcDirectory, dir.name))
        } else {
            return Completable.error(RCException("Missing manifest.yaml"))
        }
    }

    private fun validateResourceContainer(dir: File): Boolean {
        val names = dir.listFiles().map { it.name }
        return names.contains("manifest.yaml")
    }

    private fun importResourceContainer(container: File): Completable {
        val rc = ResourceContainer.load(container)
        val dc = rc.manifest.dublinCore

        if (dc.type == "bundle" && dc.format == "text/usfm") {
            expandResourceContainerBundle(rc)
        }

        val tree = constructContainerTree(rc)
        return collectionRepository.importResourceContainer(rc, tree, dc.language.identifier)
    }

    private fun constructContainerTree(rc: ResourceContainer): Tree {
        val root = constructRoot(rc)
        val nodes = getCategories(rc)
                .map { category ->
                    val categoryTree = Tree(category)
                    categoryTree.addAll(
                            getProjectsInCategory(rc.manifest, category.slug)
                                    .map {
                                        constructProjectTree(rc.dir, it, rc.type())
                                    }
                    )
                    return@map categoryTree
                }
                .plus(
                        getProjectsWithoutCategory(rc.manifest)
                                .map {
                                    constructProjectTree(rc.dir, it, rc.type())
                                }
                )
        root.addAll(nodes)
        return root
    }

    private fun constructProjectTree(containerDir: File, project: Project, type: String): Tree {
        val projectDir = File(containerDir, project.path)
        val files = projectDir.listFiles()
        val book = Tree(Collection(project.sort, project.identifier, type, project.title, null))
        val chapters = arrayListOf<Tree>()
        for (file in files) {
            val doc = ParseUsfm(file).parse()
            for (chapter in doc.chapters) {
                val slug = "${project.identifier}_${chapter.key}"
                val col = Collection(chapter.key, slug, "chapter", chapter.key.toString(), null)
                val tree = Tree(col)
                for (verse in chapter.value.values) {
                    val con = Chunk(verse.number, "verse", verse.number, verse.number, null)
                    tree.addChild(TreeNode(con))
                }
                chapters.add(tree)
            }
        }
        book.addAll(chapters)
        return book
    }


    private fun constructRoot(rc: ResourceContainer): Tree {
        val dc = rc.manifest.dublinCore
        val slug = dc.identifier
        val title = dc.title
        val label = dc.type
        val collection = Collection(0, slug, label, title, null)
        return Tree(collection)
    }

    private fun getCategories(rc: ResourceContainer): List<Collection> {
        return arrayListOf(
                Collection(1, "bible-ot", "bible-ot", "Old Testament", null),
                Collection(2, "bible-nt", "bible-nt", "New Testament", null)
        )
    }

    private fun getProjectsInCategory(manifest: Manifest, categorySlug: String): List<Project> {
        val projects = manifest.projects.filter { it.categories.contains(categorySlug) }
        return projects
    }

    private fun getProjectsWithoutCategory(manifest: Manifest): List<Project> {
        val projects = manifest.projects.filter { it.categories.isEmpty() }
        return projects
    }

    fun expandResourceContainerBundle(rc: ResourceContainer) {
        val dc = rc.manifest.dublinCore
        dc.type = "book"

        for (project in rc.manifest.projects) {
            expandUsfm(rc.dir, project)
        }

        rc.writeManifest()
    }

    fun expandUsfm(root: File, project: Project) {
        val projectRoot = File(root, project.identifier)
        projectRoot.mkdir()
        val usfmFile = File(root, project.path)
        if (usfmFile.exists() && usfmFile.extension == "usfm") {
            val book = ParseUsfm(usfmFile).parse()
            val chapterPadding = book.chapters.size.toString().length //length of the string version of the number of chapters
            val bookDir = File(root, project.identifier)
            bookDir.mkdir()
            for (chapter in book.chapters.entries) {
                val chapterFile = File(bookDir, chapter.key.toString().padStart(chapterPadding, '0') + ".usfm")
                val verses = chapter.value.entries.map { it.value }.toTypedArray()
                verses.sortBy { it.number }
                chapterFile.bufferedWriter().use {
                    it.write("\\c ${chapter.key}")
                    it.newLine()
                    for (verse in verses) {
                        it.appendln("\\v ${verse.number} ${verse.text}")
                    }
                }
            }
            usfmFile.delete()
        }
        project.path = "./${project.identifier}"
    }
}