package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.chunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

class ChunkingDemoView : View() {

    val selectedChunk: IntegerProperty = SimpleIntegerProperty(-1)

    val list = listOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {


//        chunkGrid(list)
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
    }
}

class ChunkStep() : VBox() {
    init {
        button("Blind Draft") {
            graphic = FontIcon(MaterialDesign.MDI_HEADSET)
        }

        bindSingleChild()
    }
}