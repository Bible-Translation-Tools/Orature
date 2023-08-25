package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.NodeOrientation
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.RollingSourceText
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*
import java.lang.Exception

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
    }

    init {
        tryImportStylesheet("/css/source-content.css")
    }
}

