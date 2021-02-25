package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project

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

fun Project.toCollection(): Collection = Collection(sort, identifier, "project", title, null)

fun Category.toCollection(): Collection = Collection(sort, identifier, type, title, null)
