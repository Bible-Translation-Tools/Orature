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

    private fun AssociatedAudio.progressProperty(): DoubleProperty {
        val progressProperty = SimpleDoubleProperty(0.0)
        val sub = this.selected
            .subscribe(
                {
                    progressProperty.set(if (it.value != null) 1.0 else 0.0)
                }, { e ->
                    logger.error("Error in updating resource card progress", e)
                }
            )
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
