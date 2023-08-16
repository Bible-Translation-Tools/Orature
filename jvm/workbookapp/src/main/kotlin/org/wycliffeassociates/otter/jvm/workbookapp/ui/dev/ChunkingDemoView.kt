package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

class ChunkingDemoView : View() {

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(-1)

    private val list = observableListOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        maxWidth = 300.0

        scrollpane {
            isFitToWidth = true
            prefHeight = 200.0
            vbox {
                bindChildren(list) {
                    Button(it.number.toString()).apply {
                        addClass("btn", "btn--primary")
                    }
                }
            }
            customizeScrollbarSkin()

            hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}

