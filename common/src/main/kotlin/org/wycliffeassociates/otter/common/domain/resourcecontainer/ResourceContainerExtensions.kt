package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm.ParseUsfm
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File

fun ResourceContainer.expandUSFMBundle(): Boolean {
    manifest.dublinCore.type = ContainerType.Book.slug
    for (project in manifest.projects) {
        val result = project.expandUSFMProject(file)
        if (!result) return result
    }
    writeManifest()
    return true
}

fun ResourceContainer.toCollection(): Collection {
    return Collection(
            0,
            manifest.dublinCore.identifier,
            manifest.dublinCore.type,
            manifest.dublinCore.title,
            null
    )
}

fun ResourceContainer.otterConfigCategories(): List<Category> {
    val categories = arrayListOf<Category>()
    config?.let {
        if (it is OtterResourceContainerConfig) {
            it.extendedDublinCore?.let {
                categories.addAll(it.categories)
            }
        }
    }
    return categories
}

fun Project.expandUSFMProject(root: File): Boolean {
    var result = true
    val usfmFile = root.resolve(path)
    if (usfmFile.exists() && usfmFile.extension == "usfm") {
        // Create the folder for the expanded project
        val projectDir = root.resolve(identifier)
        projectDir.mkdirs()
        try {
            // Parse the USFM
            val book = ParseUsfm(usfmFile.bufferedReader()).parse()

            val chapterPadding = book.chapters.size.toString().length // length of the string version of the number of chapters
            for (chapter in book.chapters.entries) {
                val chapterFile = projectDir.resolve(chapter.key.toString().padStart(chapterPadding, '0') + ".usfm")
                val verses = chapter.value.entries.map { it.value }.sortedBy { it.number }
                chapterFile.bufferedWriter().use {
                    it.write("\\c ${chapter.key}")
                    it.newLine()
                    for (verse in verses) {
                        it.appendln("\\v ${verse.number} ${verse.text}")
                    }
                }
            }
            usfmFile.delete()
        } catch (e: RuntimeException) {
            result = false
        }
        path = "./$identifier"
    }
    return result
}

fun Project.toCollection(): Collection = Collection(sort, identifier, "project", title, null)

fun Category.toCollection(): Collection = Collection(sort, identifier, type, title, null)