package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.application.Platform
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.skin.ScrollBarSkin
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
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
            customizeScrollThumb()

            hbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}

fun Parent.customizeScrollThumb() {
    Platform.runLater {
        val scrollBars = lookupAll(".scroll-bar")
        scrollBars
            .mapNotNull { it as? ScrollBar }
            .forEach {
                val thumb = it.lookup(".thumb")
                thumb?.add(
                    FontIcon(Material.DRAG_INDICATOR).apply {
                        addClass("thumb-icon")
                        if (it.orientation == Orientation.HORIZONTAL) rotate = 90.0
                    }
                )
            }
    }
}