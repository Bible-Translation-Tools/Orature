package org.wycliffeassociates.otter.jvm.app.ui.projectpage.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.viewmodel.ProjectPageViewModel
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesStylesheet
import org.wycliffeassociates.otter.jvm.app.widgets.*
import tornadofx.*

class ProjectPage : View() {
    private val viewModel: ProjectPageViewModel by inject()
    private var chunkGrid = createDataGrid()
    private var childrenList = ListView<Collection>()

    init {
        viewModel.setWorkspace(workspace)
    }

    override fun onDock() {
        super.onDock()
        // Make sure we refresh the chunks if need be
        // The chunk selected take could have changed since last docked
        if (viewModel.chunks.isNotEmpty()) {
            childrenList.selectedItem?.let { viewModel.selectChildCollection(it) }
        }
    }

    override val root = stackpane {
        hbox {
            vbox {
                label {
                    textProperty().bind(viewModel.projectTitleProperty)
                    addClass(ProjectPageStylesheet.projectTitle)
                }
                childrenList = listview {
                    items = viewModel.children
                    vgrow = Priority.ALWAYS
                    addClass(ProjectPageStylesheet.chapterList)
                    cellCache {
                        // TODO: Localization
                        label(it.titleKey) {
                            graphic = MaterialIconView(MaterialIcon.CHROME_READER_MODE, "20px")
                        }
                    }
                    selectionModel.selectedIndexProperty().onChange {
                        // Tell the view model which child was selected
                        if (it >= 0) viewModel.selectChildCollection(viewModel.children[it])
                    }
                }
            }

            vbox {
                hgrow = Priority.ALWAYS
                hbox {
                    style {
                        padding = box(20.px)
                    }
                    alignment = Pos.CENTER_RIGHT
                    // Back button
                    add(JFXButton(messages["back"], MaterialIconView(MaterialIcon.ARROW_BACK)).apply {
                        action {
                            workspace.navigateBack()
                        }
                        addClass(ViewTakesStylesheet.backButton)
                    })
                }
                vbox {
                    vgrow = Priority.ALWAYS
                    viewModel.contextProperty.onChange {
                        chunkGrid.removeFromParent()
                        chunkGrid = createDataGrid()
                        add(chunkGrid)
                    }
                    // Might be a better way to handle this
                    // but recreating data grid to make sure correctly styling is applied
                    viewModel.chunks.onChange {
                        chunkGrid.removeFromParent()
                        chunkGrid = createDataGrid()
                        add(chunkGrid)
                    }
                    add(chunkGrid)
                    addClass(ProjectPageStylesheet.chunkGridContainer)
                }
                listmenu {
                    orientation = Orientation.HORIZONTAL
                    useMaxWidth = true
                    style {
                        backgroundColor += Color.WHITE
                    }
                    item(graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")) {
                        activeItem = this
                        whenSelected { viewModel.changeContext(ChapterContext.RECORD) }
                        addClass(ProjectPageStylesheet.recordMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                    item(graphic = MaterialIconView(MaterialIcon.APPS, "25px")) {
                        whenSelected { viewModel.changeContext(ChapterContext.VIEW_TAKES) }
                        addClass(ProjectPageStylesheet.viewMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                    item(graphic = MaterialIconView(MaterialIcon.EDIT, "25px")) {
                        whenSelected { viewModel.changeContext(ChapterContext.EDIT_TAKES) }
                        addClass(ProjectPageStylesheet.editMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                }
            }
        }

        // Plugin active cover
        stackpane {
            style {
                alignment = Pos.CENTER
                backgroundColor += Color.BLACK
                        .deriveColor(0.0, 0.0, 0.0, 0.5)
            }
            val icon = MaterialIconView(MaterialIcon.MIC_NONE, "60px")
                    .apply {
                        style(true) {
                            fill = Color.WHITE
                        }
                        // Update the icon when the context changes
                        viewModel.contextProperty.onChange { newContext ->
                            when(newContext) {
                                ChapterContext.RECORD -> setIcon(MaterialIcon.MIC_NONE)
                                ChapterContext.EDIT_TAKES -> setIcon(MaterialIcon.EDIT)
                                else -> {}
                            }
                        }
                    }
            add(icon)
            progressindicator {
                style {
                    maxWidth = 125.px
                    maxHeight = 125.px
                    progressColor = Color.WHITE
                }
            }
            visibleProperty().bind(viewModel.showPluginActiveProperty)
        }
    }

    private fun createDataGrid(): DataGrid<Pair<Chunk, Boolean>> {
        val dataGrid = DataGrid<Pair<Chunk, Boolean>>()
        with(dataGrid) {
            items = viewModel.chunks
            vgrow = Priority.ALWAYS
            cellCache {
                val chunkCard = ChunkCard(it.first)
                when (viewModel.contextProperty.value ?: ChapterContext.RECORD) {
                    ChapterContext.RECORD -> {
                        with(chunkCard) {
                            actionButton.apply {
                                graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                                text = messages["record"]
                                addClass(ProjectPageStylesheet.recordCardButton)
                                if (it.second) addClass(ProjectPageStylesheet.hasTakes)
                            }
                        }
                    }
                    ChapterContext.VIEW_TAKES -> {
                        with(chunkCard) {
                            if (it.second) {
                                actionButton.apply {
                                    graphic = MaterialIconView(MaterialIcon.APPS)
                                    text = messages["viewTakes"]
                                    addClass(ProjectPageStylesheet.viewCardButton)
                                }
                            } else {
                                actionButton.hide()
                                chunkCard.addClass(ProjectPageStylesheet.disabledCard)
                            }
                        }
                    }
                    ChapterContext.EDIT_TAKES -> {
                        with(chunkCard) {
                            if (chunk.selectedTake != null) {
                                actionButton.apply {
                                    graphic = MaterialIconView(MaterialIcon.EDIT)
                                    text = messages["edit"]
                                    addClass(ProjectPageStylesheet.editCardButton)
                                }
                            } else {
                                actionButton.hide()
                                addClass(ProjectPageStylesheet.disabledCard)
                            }
                        }
                    }
                }
                with(chunkCard) {
                    actionButton.action {
                        viewModel.doChunkContextualAction(chunk)
                    }
                    // Add common classes
                    addClass(ProjectPageStylesheet.chunkCard)
                }
                chunkCard
            }
        }
        return dataGrid
    }
}


