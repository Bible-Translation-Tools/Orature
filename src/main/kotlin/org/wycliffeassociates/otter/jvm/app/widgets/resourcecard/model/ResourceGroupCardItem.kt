package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.workbook.*
import tornadofx.*
import tornadofx.FX.Companion.messages

data class ResourceGroupCardItem(
    val title: String,
    val resources: Observable<ResourceCardItem>
) {
    fun onRemove() {
        resources.forEach {
            it.clearDisposables()
        }
    }
}

fun resourceGroupCardItem(element: BookElement, slug: String, onSelect: (Resource) -> Unit): ResourceGroupCardItem? {
    return findResourceGroup(element, slug)?.let { rg ->
        ResourceGroupCardItem(
            getGroupTitle(element),
            getResourceCardItems(rg, onSelect)
        )
    }
}

private fun findResourceGroup(element: BookElement, slug: String): ResourceGroup? {
    return element.resources.firstOrNull {
        it.info.slug == slug
    }
}

private fun getGroupTitle(element: BookElement): String {
    return when (element) {
        is Chapter -> "${messages["chapter"]} ${element.title}"
        is Chunk -> "${messages["chunk"]} ${element.title}"
        else -> element.title
    }
}

private fun getResourceCardItems(rg: ResourceGroup, onSelect: (Resource) -> Unit): Observable<ResourceCardItem> {
    return rg.resources.map {
        ResourceCardItem(it) {
            onSelect(it)
        }
    }
}