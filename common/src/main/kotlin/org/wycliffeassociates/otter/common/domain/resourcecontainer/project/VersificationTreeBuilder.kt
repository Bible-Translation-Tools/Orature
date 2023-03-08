package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.otter.common.collections.OtterTree
import org.wycliffeassociates.otter.common.collections.OtterTreeNode
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.toCollection
import org.wycliffeassociates.otter.common.domain.versification.ParatextVersification
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.resourcecontainer.ResourceContainer

private const val FORMAT = "text/usfm"

class VersificationTreeBuilder {
    fun build(container: ResourceContainer): List<OtterTree<CollectionOrContent>> {
        val vrsFile = javaClass.classLoader.getResourceAsStream("versification/ulb_versification.json")
        val mapper = ObjectMapper(JsonFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val versification = mapper.readValue(vrsFile, ParatextVersification::class.java)

        return constructTree(versification, container)
    }

    fun constructTree(
        versification: Versification,
        container: ResourceContainer
    ): List<OtterTree<CollectionOrContent>> {
        val projectTrees: MutableList<OtterTree<CollectionOrContent>> = mutableListOf()

        for (project in container.manifest.projects) {
            val projectTree = OtterTree<CollectionOrContent>(project.toCollection())
            val chapters = versification.getChaptersInBook(project.identifier)
            for (i in 1..chapters) {
                val chapterCollection = Collection(
                    sort = i,
                    slug = "${project.identifier}_${i}",
                    labelKey = "chapter",
                    titleKey = "$i",
                    resourceContainer = null
                )
                val chapterTree = OtterTree<CollectionOrContent>(chapterCollection)
                val verses = versification.getVersesInChapter(project.identifier, i)

                val chapChunk = Content(
                    sort = 0,
                    labelKey = ContentLabel.CHAPTER.value,
                    start = 1,
                    end = verses,
                    selectedTake = null,
                    text = null,
                    format = FORMAT,
                    type = ContentType.META,
                    draftNumber = 1
                )
                chapterTree.addChild(OtterTreeNode(chapChunk))

                for (j in 1..verses) {
                    val verseChunk = Content(
                        sort = j,
                        labelKey = ContentLabel.VERSE.value,
                        start = j,
                        end = j,
                        selectedTake = null,
                        text = null,
                        format = FORMAT,
                        type = ContentType.TEXT,
                        draftNumber = 1
                    )
                    chapterTree.addChild(OtterTreeNode(verseChunk))
                }
                projectTree.addChild(chapterTree)
            }
            projectTrees.add(projectTree)
        }
        return projectTrees
    }
}