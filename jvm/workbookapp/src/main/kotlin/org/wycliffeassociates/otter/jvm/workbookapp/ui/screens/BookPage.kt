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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BookPage : Fragment() {
    private val workbookDataStore: WorkbookDataStore by inject()

    private val viewModel: BookPageViewModel by inject()

    private val tabMap: MutableMap<String, Tab> = mutableMapOf()

    override val root = JFXTabPane().apply {
        importStylesheet<MainScreenStyles>()
        importStylesheet(resources.get("/css/tab-pane.css"))
        addClass(Stylesheet.tabPane)

        // Disable builtin tab transition animation
        disableAnimationProperty().set(true)

        // Using a size property binding and toggleClass() did not work consistently. This does.
        tabs.onChange {
            if (it.list.size == 1) {
                addClass(MainScreenStyles.singleTab)
            } else {
                removeClass(MainScreenStyles.singleTab)
            }
        }
    }

    init {
        importStylesheet<CardGridStyles>()
        importStylesheet<DefaultStyles>()
    }

    override fun onDock() {
        workbookDataStore.activeChapterProperty.set(null)
        val activeResourceMetadata = workbookDataStore.activeResourceMetadataProperty.value

        createTabs()
        root.tabs.setAll(tabMap.values)

        // Adding these tabs can change the active resource property so we need to
        // change it back to what it was originally
        if (shouldRestoreActiveResourceMetadata(activeResourceMetadata)) {
            restoreActiveResourceMetadata(activeResourceMetadata)
        }
    }

    override fun onUndock() {
        tabMap.clear()
    }

    private fun shouldRestoreActiveResourceMetadata(metadataToRestore: ResourceMetadata?): Boolean {
        return metadataToRestore != null && getAssociatedMetadatas().contains(metadataToRestore)
    }

    private fun getTargetBookResourceMetadata(): ResourceMetadata {
        return workbookDataStore.workbook.target.resourceMetadata
    }

    private fun getAssociatedMetadatas(): Sequence<ResourceMetadata> {
        return sequenceOf(getTargetBookResourceMetadata()) + workbookDataStore.workbook.target.linkedResources
    }

    private fun createTabs() {
        getAssociatedMetadatas().forEach { metadata ->
            tabMap.putIfAbsent(metadata.identifier, ChapterSelectTab(metadata))
        }
    }

    private fun restoreActiveResourceMetadata(resourceMetadata: ResourceMetadata) {
        workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)
        tabMap[resourceMetadata.identifier]?.select()
    }

    private inner class ChapterSelectTab(val resourceMetadata: ResourceMetadata) : Tab() {

        val tab = buildTab()

        init {
            text = resourceMetadata.identifier

            add(tab)
            onSelected {
                viewModel.currentTabProperty.set(resourceMetadata.identifier)
                workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)
                workbookDataStore.setProjectFilesAccessor(resourceMetadata)
            }
        }

        private fun onSelected(op: () -> Unit) {
            selectedProperty().onChange { selected ->
                if (selected) {
                    op()
                }
            }
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
                                    workbookDataStore.activeChapterProperty.set(item.chapterSource)
                                    viewModel.navigate(item.chapterSource!!)
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
