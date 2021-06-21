package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.CoverArtAccessor
import org.wycliffeassociates.otter.jvm.controls.card.BookCardCell
import org.wycliffeassociates.otter.jvm.workbookapp.enums.ProjectType
import tornadofx.*
import java.util.concurrent.Callable

class BookCell(
    private val projectTypeProperty: SimpleObjectProperty<ProjectType>,
    private val existingBooks: ObservableList<Workbook> = observableListOf(),
    private val onSelected: (Collection) -> Unit
) : ListCell<Collection>() {

    private val view = BookCardCell()

    override fun updateItem(item: Collection?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            bookNameProperty.set(item.titleKey)
            projectTypeProperty.bind(this@BookCell.projectTypeProperty.stringBinding {
                it?.let { FX.messages[it.value] }
            })

            val accessor = CoverArtAccessor(item.resourceContainer!!, item.slug)
            coverArtProperty.set(accessor.getArtwork())

            setOnMouseClicked {
                onSelected(item)
            }

            disableProperty().bind(Bindings.createBooleanBinding(
                Callable {
                    existingBooks.map {
                        it.target.slug
                    }.contains(item.slug)
                },
                existingBooks
            ))
        }
    }
}
