package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChapterCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookItemModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.CardGridStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.MainScreenStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookPageViewModel
import tornadofx.*
import java.text.MessageFormat

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

    init {
        initializeProgressDialogs()
        initializeDeleteConfirmDialog()
        importStylesheet(resources.get("/css/workbook-page.css"))
    }

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

    private fun initializeDeleteConfirmDialog() {
        confirmdialog {
            this.root.prefWidthProperty().bind(
                primaryStage.widthProperty().divide(2)
            )
            this.root.prefHeightProperty().bind(
                primaryStage.heightProperty().divide(2)
            )

            messageTextProperty.set(messages["deleteProjectConfirmation"])
            confirmButtonTextProperty.set(messages["removeProject"])
            cancelButtonTextProperty.set(messages["keepProject"])

            val titleText = MessageFormat.format(
                messages["removeProjectTitle"],
                messages["remove"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.coverArtAccessor.getArtwork()
            )

            onConfirmAction {
                viewModel.showDeleteDialogProperty.set(false)
                viewModel.deleteWorkbook()
            }

            viewModel.showDeleteDialogProperty.onChange {
                if (it) open() else close()
            }

            onCloseAction { viewModel.showDeleteDialogProperty.set(false) }
            onCancelAction { viewModel.showDeleteDialogProperty.set(false) }
        }
    }

    private fun initializeProgressDialogs() {
        progressdialog {
            viewModel.showDeleteProgressDialogProperty.onChange {
                if (it) {
                    text = messages["deletingProject"]
                    graphic = FontIcon("mdi-delete")
                    open()
                } else {
                    close()
                }
            }

            viewModel.showExportProgressDialogProperty.onChange {
                if (it) {
                    text = messages["exportProject"]
                    graphic = FontIcon("mdi-share-variant")
                    open()
                } else {
                    close()
                }
            }
        }
    }

    /**
     * The tab for a single resource of the workbook. This will contain top level actions for
     * the resource, as well as the list of chapters within the resource.
     */
    private inner class WorkbookResourceTab(val resourceMetadata: ResourceMetadata) : Tab() {

        lateinit var listView: ListView<WorkbookItemModel>
        val tab = buildTab()

        init {
            text = resourceMetadata.identifier

            add(tab)
            setOnSelectionChanged {
                viewModel.openTab(resourceMetadata)
                viewModel.selectedResourceMetadata.set(resourceMetadata)
                listView.refresh()
            }

            viewModel.chapters.onChangeAndDoNow {
                val item =
                    it.singleOrNull { model ->
                        model is ChapterCardModel &&
                                model.source == viewModel.selectedChapterProperty.value
                    }
                val index = it.indexOf(item)
                listView.scrollTo(index)
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

                listView = listview(viewModel.chapters) {
                    vgrow = Priority.ALWAYS
                    addClass("workbook-page__chapter-list")

                    setCellFactory {
                        ChapterCell()
                    }
                }
            }
        }
    }
}
