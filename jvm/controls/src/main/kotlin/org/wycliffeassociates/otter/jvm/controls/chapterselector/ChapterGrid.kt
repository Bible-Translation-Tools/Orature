package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.WeakChangeListener
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

private const val DEFAULT_GRID_COLUMNS = 5

class ChapterGrid(val items: ObservableList<ChapterGridItemData> = observableListOf()) : GridPane() {
    val maxColumnProperty = SimpleIntegerProperty(DEFAULT_GRID_COLUMNS)
    val itemsProperty: ObjectProperty<ObservableList<ChapterGridItemData>> = SimpleObjectProperty(items)

    init {
        addClass("chapter-grid")
        handleItemsChange(itemsProperty.value)
        // use weak listener to avoid memory leaks
        itemsProperty.addListener(
            WeakChangeListener { _, _, newValue ->
                handleItemsChange(newValue)
            }
        )
    }

    private fun handleItemsChange(items: ObservableList<ChapterGridItemData>?) {
        this.clear()
        items?.forEachIndexed { index, chapter ->
            val node = StackPane().apply {
                button(chapter.number.toString()) {
                    addClass(
                        "btn", "btn--secondary", "btn--borderless", "chapter-grid__btn"
                    )
                    prefWidthProperty().bind(
                        this@ChapterGrid.widthProperty().divide(DEFAULT_GRID_COLUMNS.toDouble())
                    )
                }
                hbox {
                    addClass("chapter-grid__icon-alignment-box")
                    add(
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("complete-icon") }
                    )
                    isMouseTransparent = true
                    isPickOnBounds = false
                    visibleWhen { chapter.completedProperty }
                    managedWhen(visibleProperty())
                }
            }

            // add(item, column, row)
            this.add(node, index % maxColumnProperty.value, index / maxColumnProperty.value)
        }
    }
}

fun EventTarget.chapterGrid(
    list: ObservableList<ChapterGridItemData> = observableListOf(),
    op: ChapterGrid.() -> Unit = {}
) = ChapterGrid(list).attachTo(this, op)