package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*

class ChunkingDemoView : View() {

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(-1)
    private val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    private val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val showAllProperty = SimpleBooleanProperty(true)

    private val list = listOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        maxWidth = 320.0

        val grid = ChunkGrid(list)
        vbox {
            addClass("chunking-step")
            isFocusTraversable = true

            hbox {
                addClass("chunking-step__header-section")
                label {
                    addClass("chunking-step__title", "normal-text")
                    textProperty().bind(showAllProperty.stringBinding {
                        if (it == false) messages["show_completed"] else messages["hide_completed"]
                    })
                }
                region { hgrow = Priority.ALWAYS }
                label {
                    graphicProperty().bind(showAllProperty.objectBinding {
                        if (it == true) {
                            FontIcon(MaterialDesign.MDI_MENU_UP)
                        } else {
                            FontIcon(MaterialDesign.MDI_MENU_DOWN)
                        }
                    })
                }
            }

            setOnMouseClicked {
                showAllProperty.set(!showAllProperty.value)
                requestFocus()
            }
            this.addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ENTER || it.code == KeyCode.SPACE) {
                    showAllProperty.set(!showAllProperty.value)
                    requestFocus()
                }
            }
        }
        chunkingStep(ChunkingStep.CONSUME_AND_VERBALIZE,selectedStepProperty,reachableStepProperty, showAllProperty,null)
        chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, showAllProperty, null)
        chunkingStep(ChunkingStep.BLIND_DRAFT, selectedStepProperty, reachableStepProperty, showAllProperty, grid)
        chunkingStep(ChunkingStep.PEER_EDIT, selectedStepProperty, reachableStepProperty, showAllProperty, grid)
        chunkingStep(ChunkingStep.KEYWORD_CHECK, selectedStepProperty, reachableStepProperty, showAllProperty, grid)
        chunkingStep(ChunkingStep.VERSE_CHECK, selectedStepProperty, reachableStepProperty, showAllProperty, grid)
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}