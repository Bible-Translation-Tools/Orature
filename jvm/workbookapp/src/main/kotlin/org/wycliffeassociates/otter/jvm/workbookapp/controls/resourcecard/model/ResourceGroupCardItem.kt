package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model

import io.reactivex.Observable
import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.*
import tornadofx.*
import tornadofx.FX.Companion.messages

data class ResourceGroupCardItem(
    val bookElement: BookElement,
    val resources: Observable<ResourceCardItem>
) {
    val title = getGroupTitle(bookElement)

    fun onRemove() {
        resources.forEach {
            it.clearDisposables()
        }
    }

    fun groupCompletedBinding(): BooleanBinding {
        return Bindings.createBooleanBinding(
            Callable {
                resources
                    .filter { it.cardCompletedBinding().get().not() }
                    .isEmpty
                    .blockingGet()
            },
            resources.toProperty()
        )
    }
}

fun resourceGroupCardItem(
    element: BookElement,
    slug: String,
    onSelect: (BookElement, Resource) -> Unit
): ResourceGroupCardItem? {
    return findResourceGroup(element, slug)?.let { rg ->
        ResourceGroupCardItem(
            element,
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
