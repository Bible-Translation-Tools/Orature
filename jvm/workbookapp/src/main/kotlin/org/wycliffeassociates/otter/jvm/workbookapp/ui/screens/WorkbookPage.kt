/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
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
class WorkbookPage : View() {
    private val viewModel: WorkbookPageViewModel by inject()
    private val tabMap: MutableMap<String, Tab> = mutableMapOf()
    private val navigator: NavigationMediator by inject()

    private var deleteListener: ChangeListener<Boolean>? = null
    private var deleteProgressListener: ChangeListener<Boolean>? = null
    private var deleteSuccessListener: ChangeListener<Boolean>? = null
    private var deleteFailListener: ChangeListener<Boolean>? = null
    private var exportProgressListener: ChangeListener<Boolean>? = null

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            viewModel.workbookDataStore.activeChapterProperty.stringBinding {
                it?.let {
                    MessageFormat.format(
                        messages["chapterTitle"],
                        messages["chapter"],
                        it.sort
                    )
                } ?: messages["chapter"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_FILE))
        onClickAction {
            navigator.dock(this@WorkbookPage)
        }
    }

    init {
        importStylesheet(resources.get("/css/workbook-page.css"))
        importStylesheet(resources.get("/css/chapter-card.css"))
        importStylesheet(resources.get("/css/workbook-banner.css"))
        importStylesheet(resources.get("/css/confirm-dialog.css"))
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
        viewModel.workbookDataStore.activeChunkProperty.set(null)
        viewModel.workbookDataStore.activeResourceComponentProperty.set(null)
        viewModel.workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)
        selectLastResourceTab()
        initializeProgressDialogs()
        initializeDeleteConfirmDialog()
        initializeDeleteSuccessDialog()
        initializeDeleteFailDialog()
    }

    /**
     * Clear out the tabs so new ones can be created the next time this view is docked.
     */
    override fun onUndock() {
        tabMap.clear()
        removeDialogListeners()
    }

    private fun createTabs() {
        viewModel.getAllBookResources().forEach { metadata ->
            tabMap.putIfAbsent(metadata.identifier, WorkbookResourceTab(metadata))
        }
    }

    private fun selectLastResourceTab() {
        val lastResource = viewModel.getLastResource()
        tabMap.map {
            val tab = (it.value as? WorkbookResourceTab)
            if (tab?.resourceMetadata?.identifier == lastResource) {
                tab.select()
            }
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
            messageTextProperty.set(messages["deleteProjectConfirmation"])
            confirmButtonTextProperty.set(messages["removeProject"])
            cancelButtonTextProperty.set(messages["keepProject"])

            val titleText = MessageFormat.format(
                messages["removeProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)
            )

            onConfirmAction {
                viewModel.deleteWorkbook()
            }

            deleteListener = ChangeListener { _, _, new ->
                if (new) open() else close()
            }
            viewModel.showDeleteDialogProperty.addListener(deleteListener)

            onCloseAction { viewModel.showDeleteDialogProperty.set(false) }
            onCancelAction { viewModel.showDeleteDialogProperty.set(false) }
        }
    }

    private fun initializeDeleteSuccessDialog() {
        confirmdialog {
            messageTextProperty.set(messages["deleteProjectSuccess"])
            confirmButtonTextProperty.set(messages["removeProject"])
            cancelButtonTextProperty.set(messages["goHome"])

            val titleText = MessageFormat.format(
                messages["removeProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)
            )

            deleteSuccessListener = ChangeListener { _, _, new ->
                if (new) open() else close()
            }
            viewModel.showDeleteSuccessDialogProperty.addListener(deleteSuccessListener)

            onCloseAction { viewModel.goBack() }
            onCancelAction { viewModel.goBack() }
        }
    }

    private fun initializeDeleteFailDialog() {
        confirmdialog {
            messageTextProperty.set(messages["deleteProjectFail"])
            confirmButtonTextProperty.set(messages["removeProject"])
            cancelButtonTextProperty.set(messages["close"])

            val titleText = MessageFormat.format(
                messages["removeProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)
            )

            deleteFailListener = ChangeListener { _, _, new ->
                if (new) open() else close()
            }
            viewModel.showDeleteFailDialogProperty.addListener(deleteFailListener)

            onCloseAction { viewModel.showDeleteFailDialogProperty.set(false) }
            onCancelAction { viewModel.showDeleteFailDialogProperty.set(false) }
        }
    }

    private fun initializeProgressDialogs() {
        confirmdialog {
            deleteProgressListener = ChangeListener { _, _, value ->
                if (value) {
                    titleTextProperty.bind(
                        viewModel.activeProjectTitleProperty.stringBinding {
                            it?.let {
                                MessageFormat.format(
                                    messages["deleteProjectTitle"],
                                    messages["delete"],
                                    it
                                )
                            }
                        }
                    )
                    messageTextProperty.set(messages["deleteProjectMessage"])
                    backgroundImageFileProperty.bind(viewModel.activeProjectCoverProperty)
                    open()
                } else {
                    close()
                }
            }
            viewModel.showDeleteProgressDialogProperty.addListener(deleteProgressListener)

            exportProgressListener = ChangeListener { _, _, value ->
                if (value) {
                    titleTextProperty.bind(
                        viewModel.activeProjectTitleProperty.stringBinding {
                            it?.let {
                                MessageFormat.format(
                                    messages["exportProjectTitle"],
                                    messages["export"],
                                    it
                                )
                            }
                        }
                    )
                    messageTextProperty.set(
                        MessageFormat.format(
                            messages["exportProjectMessage"],
                            viewModel.activeProjectTitleProperty.value
                        )
                    )
                    backgroundImageFileProperty.bind(viewModel.activeProjectCoverProperty)
                    open()
                } else {
                    close()
                }
            }
            viewModel.showExportProgressDialogProperty.addListener(exportProgressListener)

            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)
        }
    }

    private fun removeDialogListeners() {
        viewModel.showDeleteDialogProperty.removeListener(deleteListener)
        viewModel.showDeleteProgressDialogProperty.removeListener(deleteProgressListener)
        viewModel.showDeleteFailDialogProperty.removeListener(deleteFailListener)
        viewModel.showDeleteSuccessDialogProperty.removeListener(deleteSuccessListener)
        viewModel.showExportProgressDialogProperty.removeListener(exportProgressListener)
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
