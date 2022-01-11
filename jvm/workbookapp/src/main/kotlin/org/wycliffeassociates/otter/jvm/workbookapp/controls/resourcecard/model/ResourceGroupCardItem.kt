/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model

import io.reactivex.Observable
import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.BookElement
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.common.data.workbook.ResourceGroup
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
