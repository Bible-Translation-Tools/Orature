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
    private val showAllProperty = SimpleBooleanProperty(false)
    private val isCollapsedProperty = SimpleBooleanProperty(false)

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
            visibleWhen(isCollapsedProperty.not())
            managedWhen(visibleProperty())

            hbox {
                addClass("chunking-step__header-section", "chunk-step__header-section__menu-btn")

                label {
                    addClass("chunking-step__title", "h5")
                    graphicProperty().bind(showAllProperty.objectBinding {
                        if (it == true) {
                            FontIcon(MaterialDesign.MDI_EYE_OFF).apply {
                                addClass("icon")
                            }
                        } else {
                            FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                                addClass("complete-icon")
                            }
                        }
                    })
                    textProperty().bind(showAllProperty.stringBinding {
                        if (it == true) messages["hide_completed"] else messages["show_completed"]
                    })
                }
                region { hgrow = Priority.ALWAYS }
                label {
                    addClass("chunking-step__title")
                    graphicProperty().bind(showAllProperty.objectBinding {
                        if (it == true) {
                            FontIcon(MaterialDesign.MDI_MENU_UP).apply { addClass("icon") }
                        } else {
                            FontIcon(MaterialDesign.MDI_MENU_DOWN).apply { addClass("icon") }
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

        scrollpane {
            isFitToWidth = true

            vbox {
                chunkingStep(ChunkingStep.CONSUME_AND_VERBALIZE,selectedStepProperty,reachableStepProperty, showAllProperty, isCollapsedProperty, null)
                chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, showAllProperty, isCollapsedProperty, null)
                chunkingStep(ChunkingStep.BLIND_DRAFT, selectedStepProperty, reachableStepProperty, showAllProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.PEER_EDIT, selectedStepProperty, reachableStepProperty, showAllProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.KEYWORD_CHECK, selectedStepProperty, reachableStepProperty, showAllProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.VERSE_CHECK, selectedStepProperty, reachableStepProperty, showAllProperty, isCollapsedProperty, grid)
            }
        }
        button("Collapse") {
            action {
                this@vbox.maxWidth = if (isCollapsedProperty.value) {
                    320.0
                } else {
                    80.0
                }
                isCollapsedProperty.set(!isCollapsedProperty.value)
            }
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}