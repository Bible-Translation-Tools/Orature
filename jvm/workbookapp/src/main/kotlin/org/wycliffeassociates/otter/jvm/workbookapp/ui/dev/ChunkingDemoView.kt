package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Button
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class ChunkingDemoView : View() {
    class ChunkViewData(val number: Int, val completed: BooleanProperty, val selectedChunk: IntegerProperty)

    val selectedChunk: IntegerProperty = SimpleIntegerProperty(-1)

    val buttonLabels = listOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        gridpane {

            buttonLabels.forEachIndexed { index, chunk ->
                val btn = Button(chunk.number.toString()).apply {
                    addClass("btn", "btn--secondary", "btn--borderless", "chunk-item")
                    graphicProperty().bind(chunk.completed.objectBinding {
                        this.togglePseudoClass("completed", it == true)
                        if (it == true) {
                            FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("chunk-item__icon") }
                        } else {
                            FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply { addClass("chunk-item__icon") }
                        }
                    })

                    chunk.selectedChunk.onChange {
                        this.togglePseudoClass("selected", it == chunk.number)
                    }

                    action {
                        chunk.selectedChunk.set(chunk.number)
                    }
                }

                this.add(btn, index % 3, index / 3)
            }
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
    }
}