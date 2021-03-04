package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.card.card
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.CardGridStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.MainScreenStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookPageViewModel
import tornadofx.*

class BookPage : Fragment() {
    private val viewModel: BookPageViewModel by inject()
    private val tabMap: MutableMap<String, Tab> = mutableMapOf()

    override fun onDock() {
        viewModel.openBook()
        createTabs()
        root.tabs.setAll(tabMap.values)
    }

    override fun onUndock() {
        tabMap.clear()
    }

    private fun createTabs() {
        viewModel.getAssociatedMetadata().forEach { metadata ->
            tabMap.putIfAbsent(metadata.identifier, ChapterSelectTab(metadata))
        }
    }

    override val root = JFXTabPane().apply {
        importStylesheet<CardGridStyles>()
        importStylesheet<DefaultStyles>()
        importStylesheet<MainScreenStyles>()
        importStylesheet(resources.get("/css/tab-pane.css"))
        addClass(Stylesheet.tabPane)

        tabs.onChange {
            when (it.list.size) {
                1 -> addClass(MainScreenStyles.singleTab)
                else -> removeClass(MainScreenStyles.singleTab)
            }
        }
    }

    private inner class ChapterSelectTab(val resourceMetadata: ResourceMetadata) : Tab() {

        val tab = buildTab()

        init {
            text = resourceMetadata.identifier

            add(tab)
            setOnSelectionChanged {
                viewModel.openTab(resourceMetadata)
            }
        }

        fun buildTab(): VBox {
            return VBox().apply {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                addClass(AppStyles.whiteBackground)
                progressindicator {
                    visibleProperty().bind(viewModel.loadingProperty)
                    managedProperty().bind(visibleProperty())
                    addClass(CardGridStyles.contentLoadingProgress)
                }

                datagrid(viewModel.allContent) {
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS
                    isFillWidth = true
                    addClass(AppStyles.whiteBackground)
                    addClass(CardGridStyles.contentContainer)
                    cellCache { item ->
                        card {
                            addClass(DefaultStyles.defaultCard)
                            cardfront {
                                innercard(cardGraphic()) {
                                    title = item.item.toUpperCase()
                                    bodyText = item.bodyText
                                }
                                cardbutton {
                                    addClass(DefaultStyles.defaultCardButton)
                                    text = messages["openProject"]
                                    graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                        .apply { fill = AppTheme.colors.appRed }
                                    onMousePressed = EventHandler {
                                        item.chapterSource?.let { chapter ->
                                            viewModel.navigate(chapter)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cardGraphic(): Node {
        return AppStyles.chapterGraphic()
    }
}
