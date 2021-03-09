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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookPageViewModel
import tornadofx.*

/**
 * The page for an open Workbook (project).
 *
 * A Workbook is the combination of the book being translated/recorded, as well as any supplemental
 * study resources that can be translated/recorded. For example, a Workbook for the book of Matthew
 * would contain the book of Matthew (of a particular publication, such as the Unlocked Literal Bible),
 * as well as (optionally) resources such as translationQuestions and translationNotes for the book of
 * Matthew.
 *
 * This page contains a tab for each resource in the workbook. If the workbook only contains the book
 * itself, then no tabs will be shown.
 */
class WorkbookPage : Fragment() {
    private val viewModel: WorkbookPageViewModel by inject()
    private val tabMap: MutableMap<String, Tab> = mutableMapOf()

    /**
     * On docking, notify the viewmodel (which may be reused and thus dirty) that we are
     * opening a workbook (which it will retrieve from the WorkbookDataStore). Tabs are then
     * created and added to the view.
     */
    override fun onDock() {
        viewModel.openWorkbook()
        createTabs()
        root.tabs.setAll(tabMap.values)
    }

    /**
     * Clear out the tabs so new ones can be created the next time this view is docked.
     */
    override fun onUndock() {
        tabMap.clear()
    }

    private fun createTabs() {
        viewModel.getAllBookResources().forEach { metadata ->
            tabMap.putIfAbsent(metadata.identifier, WorkbookResourceTab(metadata))
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

    /**
     * The tab for a single resource of the workbook. This will contain top level actions for
     * the resource, as well as the list of chapters within the resource.
     */
    private inner class WorkbookResourceTab(val resourceMetadata: ResourceMetadata) : Tab() {

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

                datagrid(viewModel.chapters) {
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

        private fun cardGraphic(): Node {
            return AppStyles.chapterGraphic()
        }
    }
}
