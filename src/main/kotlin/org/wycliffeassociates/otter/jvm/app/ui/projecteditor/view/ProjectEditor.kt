package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel.ProjectEditorViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectPageStylesheet
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesStylesheet
import org.wycliffeassociates.otter.jvm.app.widgets.ChunkCard
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog
import tornadofx.*

class ProjectEditor : View() {
    private val viewModel: ProjectEditorViewModel by inject()
    private var childrenList by singleAssign<ListView<Collection>>()

    override fun onDock() {
        super.onDock()
        // Make sure we refresh the chunks if need be
        // The chunk selected take could have changed since last docked
        viewModel.chunks.forEach {
            // null the chunk and then reassign it to force
            // property on change to be called and update the bound card
            val tmp = it.first.value
            it.first.value = null
            it.first.value = tmp
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
                    alignment = Pos.CENTER
                    progressindicator {
                        visibleProperty().bind(viewModel.loadingProperty)
                        managedProperty().bind(visibleProperty())
                        addClass(ProjectPageStylesheet.chunkLoadingProgress)
                    }
                    datagrid(viewModel.chunks) {
                        vgrow = Priority.ALWAYS
                        visibleProperty().bind(viewModel.loadingProperty.toBinding().not())
                        managedProperty().bind(visibleProperty())
                        cellCache { item ->
                            val chunkCard = ChunkCard()
                            chunkCard.chunkProperty().bind(item.first)
                            chunkCard.bindClass(cardContextCssRuleProperty())
                            chunkCard.bindClass(disabledCssRuleProperty(item.second))
                            chunkCard.bindClass(hasTakesCssRuleProperty(item.second))
                            viewModel.contextProperty.toObservable().subscribe { context ->
                                chunkCard.actionButton.isVisible =
                                        (item.second.value == true || context == ChapterContext.RECORD)
                                when (context ?: ChapterContext.RECORD) {
                                    ChapterContext.RECORD -> {
                                        chunkCard.actionButton.apply {
                                            graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                                            text = messages["record"]
                                        }
                                    }
                                    ChapterContext.VIEW_TAKES -> {
                                        chunkCard.actionButton.apply {
                                            graphic = MaterialIconView(MaterialIcon.APPS)
                                            text = messages["viewTakes"]
                                        }
                                    }
                                    ChapterContext.EDIT_TAKES -> {
                                        chunkCard.actionButton.apply {
                                            graphic = MaterialIconView(MaterialIcon.EDIT)
                                            text = messages["edit"]
                                        }
                                    }
                                }
                            }
                            chunkCard.actionButton.action { viewModel.doChunkContextualAction(chunkCard.chunk) }
                            // Add common classes
                            chunkCard.addClass(ProjectPageStylesheet.chunkCard)
                            return@cellCache chunkCard
                        }
                    }
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
        val dialog = progressdialog {
            graphic = MaterialIconView(MaterialIcon.MIC_NONE, "60px")
            viewModel.contextProperty.onChange { newContext ->
                when (newContext) {
                    ChapterContext.RECORD -> graphic = MaterialIconView(MaterialIcon.MIC_NONE, "60px")
                    ChapterContext.EDIT_TAKES -> graphic = MaterialIconView(MaterialIcon.EDIT, "60px")
                    else -> { }
                }
            }
        }
        viewModel.showPluginActiveProperty.onChange {
            Platform.runLater { if (it == true) dialog.open() else dialog.close() }
        }
    }

    private fun cardContextCssRuleProperty(): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        viewModel.contextProperty.toObservable().subscribe {
            cssRuleProperty.value = when (it ?: ChapterContext.RECORD) {
                ChapterContext.RECORD -> ProjectPageStylesheet.recordContext
                ChapterContext.VIEW_TAKES -> ProjectPageStylesheet.viewContext
                ChapterContext.EDIT_TAKES -> ProjectPageStylesheet.editContext
            }
        }
        return cssRuleProperty
    }

    private fun hasTakesCssRuleProperty(
            hasTakesProperty: SimpleBooleanProperty
    ): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        hasTakesProperty.toObservable().subscribe {
            cssRuleProperty.value = if (it == true) ProjectPageStylesheet.hasTakes else null
        }
        return cssRuleProperty
    }

    private fun disabledCssRuleProperty(
            hasTakesProperty: SimpleBooleanProperty
    ): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        viewModel.contextProperty.toObservable().subscribe {
            cssRuleProperty.value = when (it ?: ChapterContext.RECORD) {
                ChapterContext.RECORD -> null
                ChapterContext.VIEW_TAKES, ChapterContext.EDIT_TAKES -> {
                    if (hasTakesProperty.value) {
                        null
                    } else {
                        ProjectPageStylesheet.disabledCard
                    }
                }
            }
        }
        return cssRuleProperty
    }
}



