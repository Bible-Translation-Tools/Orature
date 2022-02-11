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

import io.reactivex.disposables.CompositeDisposable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.commonmark.parser.Parser
import org.commonmark.renderer.text.TextContentRenderer
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Take
import java.util.concurrent.Callable

data class ResourceCardItem(val resource: Resource, val onSelect: () -> Unit) {

    private val logger = LoggerFactory.getLogger(ResourceCardItem::class.java)

    val title: String = renderTitleAsPlainText()
    private val disposables = CompositeDisposable()
    val titleProgressProperty: DoubleProperty = resource.title.audio.progressProperty()
    val bodyProgressProperty: DoubleProperty? = resource.body?.audio?.progressProperty()
    val hasBodyAudio: Boolean = resource.body?.audio != null

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        clearDisposables()
    }

    fun clearDisposables() {
        disposables.clear()
    }

    private fun Take.isNotDeleted() = deletedTimestamp.value?.value == null

    private fun AssociatedAudio.progressProperty(): DoubleProperty {
        val progressProperty = SimpleDoubleProperty(0.0)
        val sub = this.selected
            .doOnError { e ->
                logger.error("Error in updating resource card progress", e)
            }
            .subscribe {
                val completed = it.value?.isNotDeleted() ?: false
                progressProperty.set(if (completed) 1.0 else 0.0)
            }
        disposables.add(sub)
        return progressProperty
    }

    companion object {
        val parser: Parser = Parser.builder().build()
        val renderer: TextContentRenderer = TextContentRenderer.builder().build()
    }

    private fun renderTitleAsPlainText(): String {
        val document = parser.parse(resource.title.textItem.text)
        return renderer.render(document)
    }

    fun cardCompletedBinding(): BooleanBinding {
        val dependencies = arrayOf(titleProgressProperty)
        bodyProgressProperty?.let { dependencies.plus(it) }

        return Bindings.createBooleanBinding(
            Callable {
                val titleCompleted = titleProgressProperty.get() == 1.0
                val bodyCompleted =
                    bodyProgressProperty == null || bodyProgressProperty.get() == 1.0
                titleCompleted && bodyCompleted
            },
            *dependencies
        )
    }
}
