/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.enableContentAnimation
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChapterCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ContributorInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
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
    private val workbookDataStore: WorkbookDataStore by inject()
    private val settingsViewModel: SettingsViewModel by inject()

    private var deleteListener: ChangeListener<Boolean>? = null
    private var deleteProgressListener: ChangeListener<Boolean>? = null
    private var deleteSuccessListener: ChangeListener<Boolean>? = null
    private var deleteFailListener: ChangeListener<Boolean>? = null
    private var exportProgressListener: ChangeListener<Boolean>? = null
    private val tabChaptersListeners = mutableMapOf<String, ListChangeListener<ChapterCardModel>>()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeWorkbookProperty.stringBinding {
                it?.target?.title
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOK))
        setOnAction {
            navigator.dock(this@WorkbookPage)
        }
    }

    init {
        tryImportStylesheet(resources["/css/workbook-page.css"])
        tryImportStylesheet(resources["/css/chapter-card.css"])
        tryImportStylesheet(resources["/css/workbook-banner.css"])
        tryImportStylesheet(resources["/css/confirm-dialog.css"])
        tryImportStylesheet(resources["/css/contributor-info.css"])
    }

    /**
     * On docking, notify the viewmodel (which may be reused and thus dirty) that we are
     * opening a workbook (which it will retrieve from the WorkbookDataStore). Tabs are then
     * created and added to the view.
     */
    override fun onDock() {
        viewModel.dock()
        createTabs()
        root.tabs.setAll(tabMap.values)
        viewModel.openWorkbook()
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
        super.onUndock()
        tabMap.forEach { _, tab ->
            (tab as WorkbookResourceTab).undock()
        }
        tabMap.clear()
        removeDialogListeners()
        tabChaptersListeners.map {
            viewModel.chapters.removeListener(it.value)
        }
        tabChaptersListeners.clear()
        viewModel.undock()
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

    override val root = TabPane().apply {
        addClass("wa-tab-pane")
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabs.onChange {
            when (it.list.size) {
                1 -> addClass("singleTab")
                else -> removeClass("singleTab")
            }
        }
        focusTraversableProperty().bind(
            Bindings.createBooleanBinding(
                { tabs.count() > 1 },
                tabs
            )
        )
        enableContentAnimation()
    }

    private fun initializeDeleteConfirmDialog() {
        confirmdialog {
            messageTextProperty.set(messages["deleteProjectConfirmation"])
            confirmButtonTextProperty.set(messages["removeProject"])
            cancelButtonTextProperty.set(messages["keepProject"])
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)

            val titleText = MessageFormat.format(
                messages["deleteProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)?.file
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
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)

            val titleText = MessageFormat.format(
                messages["deleteProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)?.file
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
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)

            val titleText = MessageFormat.format(
                messages["deleteProjectTitle"],
                messages["delete"],
                viewModel.workbookDataStore.workbook.target.title
            )

            titleTextProperty.set(titleText)
            backgroundImageFileProperty.set(
                viewModel.workbookDataStore.workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)?.file
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

                    viewModel.activeProjectTitleProperty.stringBinding {
                        it?.let {
                            MessageFormat.format(
                                messages["exportProjectMessage"],
                                it
                            )
                        }
                    }.onChangeAndDoNow { messageTextProperty.set(it) }

                    backgroundImageFileProperty.bind(viewModel.activeProjectCoverProperty)
                    open()
                } else {
                    close()
                }
            }
            viewModel.showExportProgressDialogProperty.addListener(exportProgressListener)

            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.set(settingsViewModel.appColorMode.value)
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

        lateinit var listView: ListView<ChapterCardModel>
        lateinit var banner: WorkbookBanner
        val tab = buildTab()

        init {
            text = resourceMetadata.identifier
            content = tab

            whenSelected {
                viewModel.openTab(resourceMetadata)
                viewModel.selectedResourceMetadata.set(resourceMetadata)
                listView.refresh()
            }

            tabChaptersListeners.putIfAbsent(text, ListChangeListener {
                val index = workbookDataStore.workbookRecentChapterMap.getOrDefault(
                    workbookDataStore.workbook.hashCode(),
                    -1
                )

                listView.selectionModel.select(index)
                runLater {
                    listView.requestFocus()
                    listView.focusModel.focus(index)
                    listView.scrollTo(index)
                }
            })
            viewModel.chapters.addListener(tabChaptersListeners[text])
        }

        fun buildTab(): VBox {
            return VBox().apply {
                addClass("workbook-page__tab-content")
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
                alignment = Pos.CENTER

                progressindicator {
                    visibleProperty().bind(viewModel.loadingProperty)
                    managedProperty().bind(visibleProperty())
                    addClass("workbook-page__content-loading-progress")
                }

                hbox {
                    vgrow = Priority.ALWAYS

                    vbox {
                        hgrow = Priority.ALWAYS
                        fitToParentWidth()
                        addClass("workbook-page__left-pane")

                        add(
                            buildWorkbookBanner().apply {
                                banner = this
                            }
                        )

                        listview(viewModel.chapters) {
                            listView = this
                            vgrow = Priority.ALWAYS

                            addClass("wa-list-view", "workbook-page__chapter-list")

                            setCellFactory {
                                ChapterCell()
                            }
                        }
                    }

                    add(buildContributorSection())
                }
            }
        }

        fun undock() {
            banner.cleanUp()
        }

        private fun buildWorkbookBanner(): WorkbookBanner {
            return WorkbookBanner().apply {
                val workbook = workbookDataStore.activeWorkbookProperty
                backgroundArtworkProperty.bind(
                    workbook.objectBinding {
                        it?.artworkAccessor?.getArtwork(ImageRatio.TWO_BY_ONE)
                    }
                )
                bookTitleProperty.bind(workbook.stringBinding { it?.target?.title })
                resourceTitleProperty.bind(viewModel.selectedResourceMetadata.stringBinding { it?.title })
                isBookResourceProperty.bind(
                    viewModel.selectedResourceMetadata.booleanBinding {
                        it?.type == ContainerType.Book
                    }
                )
                hideDeleteButtonProperty.bind(isBookResourceProperty.not())
                deleteTitleProperty.set(FX.messages["delete"])
                exportTitleProperty.bind(
                    Bindings.createStringBinding(
                        {
                            when (viewModel.selectedResourceMetadata.value?.type) {
                                ContainerType.Book, ContainerType.Bundle -> FX.messages["exportProject"]
                                ContainerType.Help -> FX.messages["exportResource"]
                                else -> ""
                            }
                        },
                        viewModel.selectedResourceMetadata
                    )
                )
                onDeleteAction { viewModel.showDeleteDialogProperty.set(true) }
                onExportAction { option ->
                    val directory = chooseDirectory(FX.messages["exportProject"])
                    directory?.let { dir ->
                        viewModel.exportWorkbook(dir, option)
                    }
                }
            }
        }

        private fun buildContributorSection(): ContributorInfo {
            return ContributorInfo(viewModel.contributors).apply {
                hgrow = Priority.SOMETIMES

                visibleWhen {
                    currentStage!!.widthProperty().greaterThan(minWidthProperty() * 2)
                }
                managedWhen(visibleProperty())

                addContributorCallbackProperty.set(
                    EventHandler {
                        viewModel.addContributor(it.source as String)
                    }
                )
                editContributorCallbackProperty.set(
                    EventHandler {
                        val data = it.source as ContributorCellData
                        viewModel.editContributor(data)
                        lastModifiedIndex.set(data.index)
                    }
                )
                removeContributorCallbackProperty.set(
                    EventHandler {
                        val indexToRemove = it.source as Int
                        viewModel.removeContributor(indexToRemove)
                    }
                )
                button(messages["saveContributors"]) {
                    addClass("btn", "btn--primary", "btn--borderless", "contributor__save-btn")
                    fitToParentWidth()
                    tooltip(this.text)
                    isDisable = true
                    viewModel.contributors.onChange { isDisable = false }

                    setOnAction {
                        viewModel.saveContributorInfo()
                        isDisable = true
                    }
                }
                vbox {
                    label(messages["licenseDescription"]) {
                        addClass("contributor__section-text")
                        minHeight = Region.USE_PREF_SIZE // prevent overflow
                    }
                    hyperlink(messages["licenseCCBYSA"]) {
                        addClass("wa-text--hyperlink","contributor__license-link")
                        minHeight = Region.USE_PREF_SIZE // prevent overflow

                        val url = "https://creativecommons.org/licenses/by-sa/4.0/"
                        tooltip(url)
                        action {
                            FX.application.hostServices.showDocument(url)
                        }
                    }
                }
            }
        }
    }
}
