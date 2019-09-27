package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.model.ContentLabel
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

fun resourceGroupCardItem(
    element: BookElement,
    slug: String,
    onSelect: (BookElement, Resource) -> Unit
): ResourceGroupCardItem? {
    return findResourceGroup(element, slug)?.let { rg ->
        ResourceGroupCardItem(
            getGroupTitle(element),
            getResourceCardItems(rg, element, onSelect)
        )
    }
}

private fun findResourceGroup(element: BookElement, slug: String): ResourceGroup? {
    return element.resources.firstOrNull {
        it.metadata.identifier == slug
    }
}

private fun getGroupTitle(element: BookElement): String {
    return when (element) {
        is Chapter -> "${messages[ContentLabel.CHAPTER.value]} ${element.title}"
        is Chunk -> "${messages["chunk"]} ${element.title}"
        else -> element.title
    }
}

private fun getResourceCardItems(
    rg: ResourceGroup,
    bookElement: BookElement,
    onSelect: (BookElement, Resource) -> Unit
): Observable<ResourceCardItem> {
    return rg.resources.map {
        ResourceCardItem(it) {
            onSelect(bookElement, it)
        }
    }
}