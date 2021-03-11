package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
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

    private val confirmDialog = confirmdialog {
        this.root.prefWidthProperty().bind(
            this@WorkbookPage.root.widthProperty().divide(2)
        )
        this.root.prefHeightProperty().bind(
            this@WorkbookPage.root.heightProperty().divide(2)
        )

        messageTextProperty.set(messages["deleteProjectConfirmation"])
        confirmButtonTextProperty.set(messages["removeProject"])
        cancelButtonTextProperty.set(messages["keepProject"])

        onCloseAction { close() }
        onCancelAction { close() }
    }

    private fun showDeleteConfirmDialog() {
        val workbook = viewModel.workbookDataStore.workbook
        confirmDialog.apply {
            val titleText = MessageFormat.format(
                messages["removeProjectTitle"],
                messages["remove"],
                workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(workbook.coverArtAccessor.getArtwork())

            onConfirmAction {
                Platform.runLater { close() }
                viewModel.deleteWorkbook()
            }
        }.open()
    }

    private fun initializeProgressDialogs() {
        progressdialog {
            viewModel.showDeleteDialogProperty.onChange {
                if (it) {
                    text = messages["deletingProject"]
                    graphic = FontIcon("mdi-delete")
                    open()
                } else {
                    close()
                }
            }

            viewModel.showExportDialogProperty.onChange {
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

                scrollpane {
                    vgrow = Priority.ALWAYS

                    isFitToWidth = true
                    isFitToHeight = true

                    vbox {
                        maxWidth = 800.0
                        spacing = 10.0
                        padding = Insets(10.0, 20.0, 10.0, 20.0)

                        add(
                            WorkbookBanner().apply {
                                val workbook = viewModel.workbookDataStore.workbook

                                backgroundImageFileProperty.set(workbook.coverArtAccessor.getArtwork())
                                bookTitleProperty.set(workbook.target.title)
                                resourceTitleProperty.set(resourceMetadata.title)

                                deleteTitleProperty.set(messages["delete"])

                                exportTitleProperty.set(
                                    when (resourceMetadata.type) {
                                        ContainerType.Book, ContainerType.Bundle -> messages["exportProject"]
                                        ContainerType.Help -> messages["exportResource"]
                                        else -> ""
                                    }
                                )

                                onDeleteAction {
                                    showDeleteConfirmDialog()
                                }

                                onExportAction {
                                    val directory = chooseDirectory(FX.messages["exportProject"])
                                    directory?.let {
                                        viewModel.exportWorkbook(it)
                                    }
                                }
                            }
                        )

                        viewModel.chapters.onChangeAndDoNow {
                            it.forEach { item ->
                                add(
                                    ChapterCard().apply {
                                        titleProperty.set(item.sort.toString())

                                        onMousePressed = EventHandler {
                                            item.chapterSource?.let { chapter ->
                                                viewModel.navigate(chapter)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
