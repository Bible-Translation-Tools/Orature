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

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ExportChapterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import java.util.*
import kotlin.math.max

class ChapterPage : View() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: ChapterPageViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val navigator: NavigationMediator by inject()
    private lateinit var chunkListView: ListView<CardData>

    private val pluginOpenedPage: PluginOpenedPage
    private var exportProgressListener: ChangeListener<Boolean>? = null

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeChapterProperty.stringBinding {
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
        setOnAction {
            navigator.dock(this@ChapterPage)
        }
    }

    override fun onDock() {
        super.onDock()
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)

        viewModel.dock()
        viewModel.setWorkChunk()
        viewModel.openPlayers()

        viewModel.checkCanCompile()
        chunkListView.refresh()
        initializeProgressDialog()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.closePlayers()
        viewModel.undock()
        removeDialogListeners()
        (app as IDependencyGraphProvider).dependencyGraph.injectConnectionFactory().releasePlayer()
        (app as IDependencyGraphProvider).dependencyGraph.injectConnectionFactory().clearPlayerConnections()
    }

    init {
        tryImportStylesheet(resources.get("/css/chapter-page.css"))
        tryImportStylesheet(resources.get("/css/chunk-item.css"))
        tryImportStylesheet(resources.get("/css/take-item.css"))
        tryImportStylesheet(resources.get("/css/add-plugin-dialog.css"))
        tryImportStylesheet(resources.get("/css/confirm-dialog.css"))
        tryImportStylesheet(resources.get("/css/contributor-info.css"))

        pluginOpenedPage = createPluginOpenedPage()
        workspace.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
                viewModel.openPlayers()
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
        }
    }

    override val root = hbox {
        addClass("chapter-page")

        createSnackBar()

        vbox {
            addClass("chapter-page__chapter-info")
            vgrow = Priority.ALWAYS

            vbox {
                addClass("chapter-page__chapter-box")
                vgrow = Priority.ALWAYS

                label {
                    addClass("chapter-page__chapter-title")
                    textProperty().bind(viewModel.chapterCardProperty.stringBinding {
                        MessageFormat.format(
                            FX.messages["chapterTitle"],
                            FX.messages["chapter"].capitalizeString(),
                            it?.bodyText
                        )
                    })
                }

                hbox {
                    addClass("chapter-page__chapter-audio")
                    vgrow = Priority.ALWAYS

                    simpleaudioplayer {
                        hgrow = Priority.ALWAYS
                        playerProperty.bind(workbookDataStore.selectedChapterPlayerProperty)
                        visibleWhen(playerProperty.isNotNull)
                        managedProperty().bind(visibleProperty())
                    }

                    hbox {
                        hgrow = Priority.ALWAYS
                        addClass("chapter-page__not-started")

                        label(messages["draftingNotStarted"])

                        visibleWhen(workbookDataStore.selectedChapterPlayerProperty.isNull)
                        managedProperty().bind(visibleProperty())
                    }
                }

                hbox {
                    addClass("chapter-page__chapter-actions")
                    button {
                        addClass("btn", "btn--secondary")
                        text = messages["recordChapter"]
                        tooltip(text)
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        action {
                            viewModel.recordChapter()
                        }
                        shortcut(Shortcut.RECORD.value)
                    }
                    button {
                        addClass("btn", "btn--secondary")
                        text = messages["exportChapter"]
                        tooltip(text)
                        graphic = FontIcon(Material.UPLOAD_FILE)
                        action {
                            find<ExportChapterDialog>().apply {
                                orientationProperty.set(settingsViewModel.orientationProperty.value)
                            }.open()
                        }
                        disableProperty().bind(viewModel.selectedChapterTakeProperty.isNull)
                    }
                }
            }

            vbox {
                addClass("chapter-page__actions")
                vgrow = Priority.ALWAYS

                button {
                    addClass("btn", "btn--primary")
                    text = messages["editChapter"]
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                    enableWhen(viewModel.selectedChapterTakeProperty.isNotNull)
                    action {
                        viewModel.processTakeWithPlugin(PluginType.EDITOR)
                    }
                }
                button {
                    addClass("btn", "btn--primary")
                    text = messages["markAction"]
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_BOOKMARK)
                    enableWhen(viewModel.selectedChapterTakeProperty.isNotNull)
                    action {
                        viewModel.processTakeWithPlugin(PluginType.MARKER)
                    }
                }
                button {
                    addClass("btn", "btn--primary")
                    text = messages["viewTakes"]
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC)
                    action {
                        viewModel.chapterCardProperty.value?.let {
                            viewModel.onCardSelection(it)
                            navigator.dock<RecordScripturePage>()
                        }
                    }
                }
            }
        }

        stackpane {
            vbox {
                addClass("chapter-page__chunks")
                vgrow = Priority.ALWAYS

                hbox {
                    addClass("chapter-page__chunks-header")
                    button {
                        addClass("btn", "btn--secondary", "btn--secondary-light")
                        text = messages["compile"]
                        tooltip(text)
                        graphic = FontIcon(MaterialDesign.MDI_LAYERS)
                        enableWhen(viewModel.canCompileProperty.and(viewModel.isCompilingProperty.not()))
                        action {
                            viewModel.compile()
                        }
                    }
                    region {
                        hgrow = Priority.ALWAYS
                    }
                    button {
                        addClass("btn", "btn--cta")
                        textProperty().bind(viewModel.noTakesProperty.stringBinding {
                            when (it) {
                                true -> messages["beginTranslation"]
                                else -> messages["continueTranslation"]
                            }
                        })
                        tooltip { textProperty().bind(this@button.textProperty()) }
                        graphic = FontIcon(MaterialDesign.MDI_VOICE)
                        action {
                            viewModel.workChunkProperty.value?.let {
                                viewModel.onCardSelection(it)
                                navigator.dock<RecordScripturePage>()
                            }
                        }
                    }
                }

                listview(viewModel.filteredContent) {
                    addClass("wa-list-view")
                    chunkListView = this
                    fitToParentHeight()

                    val focusedChunkProperty = SimpleObjectProperty<ChunkItem>()

                    setCellFactory {
                        ChunkCell(
                            settingsViewModel.orientationScaleProperty.value,
                            focusedChunkProperty
                        )
                    }

                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        when (it.code) {
                            KeyCode.TAB, KeyCode.DOWN, KeyCode.UP -> {
                                val delta = if (it.isShiftDown || it.code == KeyCode.UP) -1 else 1
                                scrollListTo(delta)
                            }
                        }
                    }
                }
            }
            vbox {
                addClass("chapter-page__chunks")
                vgrow = Priority.ALWAYS

                var textBlock1: Label? = null
                var textBlock2: Label? = null

                hbox {
                    addClass("chunk-mode")
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS

                    vbox {
                        addClass("chunk-mode__selection-card")
                        vgrow = Priority.ALWAYS
                        prefWidthProperty().bind(this@hbox.widthProperty().divide(2))

                        vbox {
                            add(
                                FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply {
                                    addClass("chunk-mode__icon")
                                }
                            )
                            label("Verse by Verse") {
                                addClass("chunk-mode__selection__title")
                            }
                            label("Start a new translation with the default verse structure.") {
                                addClass("chunk-mode__selection__text")
                                textBlock1 = this
                            }
                        }

                        button("Select") {
                            addClass("btn", "btn--secondary", "chunk-mode__selection-btn")
                            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        }
                    }

                    vbox {
                        addClass("chunk-mode__selection-card")
                        vgrow = Priority.ALWAYS
                        prefWidthProperty().bind(this@hbox.widthProperty().divide(2))

                        vbox {
                            add(
                                FontIcon(MaterialDesign.MDI_FLAG).apply {
                                    addClass("chunk-mode__icon")
                                }
                            )
                            label("Chunks") {
                                addClass("chunk-mode__selection__title")
                            }
                            label("Start a new translation with custom chunk markers.") {
                                addClass("chunk-mode__selection__text")
                                textBlock2 = this
                            }
                        }

                        button("Select") {
                            addClass("btn", "btn--secondary", "chunk-mode__selection-btn")
                            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        }
                    }

                    textBlock1?.let { text1 ->
                        textBlock2?.let { text2 ->
                            // bind height to the tallest block's height by text size (it wraps)
                            if (text1.text.length > text2.text.length) {
                                text2.prefHeightProperty().bind(text1.heightProperty())
                            } else {
                                text1.prefHeightProperty().bind(text2.heightProperty())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ListView<CardData>.scrollListTo(delta: Int) {
        if (selectionModel.selectedIndex == 0) return
        val current = max(0, selectionModel.selectedIndex)
        val index = current + delta
        virtualFlow().scrollTo(index)
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return find<PluginOpenedPage>().apply {
            dialogTitleProperty.bind(viewModel.dialogTitleBinding())
            dialogTextProperty.bind(viewModel.dialogTextBinding())
            playerProperty.bind(viewModel.sourceAudioPlayerProperty)
            targetAudioPlayerProperty.bind(workbookDataStore.targetAudioProperty.objectBinding { it?.player })
            audioAvailableProperty.bind(viewModel.sourceAudioAvailableProperty)
            licenseProperty.bind(workbookDataStore.sourceLicenseProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
            orientationProperty.bind(settingsViewModel.orientationProperty)
            sourceOrientationProperty.bind(settingsViewModel.sourceOrientationProperty)

            sourceSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.sourceRate.toLazyBinding()
                }
            )

            targetSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.targetRate.toLazyBinding()
                }
            )
            sourceTextZoomRateProperty.bind(
                workbookDataStore.sourceTextZoomRateProperty
            )
        }
    }

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(
                            pluginErrorMessage,
                            messages["addApp"].uppercase(Locale.getDefault())
                        ) {
                            audioPluginViewModel.addPlugin(true, false)
                        },
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }

    private fun initializeProgressDialog() {
        confirmdialog {
            exportProgressListener = ChangeListener { _, _, value ->
                if (value) {
                    titleTextProperty.bind(
                        workbookDataStore.activeChapterProperty.stringBinding {
                            it?.let {
                                MessageFormat.format(
                                    messages["exportChapterTitle"],
                                    messages["export"],
                                    messages[it.label],
                                    it.title
                                )
                            }
                        }
                    )
                    messageTextProperty.set(
                        MessageFormat.format(
                            messages["exportProjectMessage"],
                            messages["chapter"]
                        )
                    )
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
        viewModel.showExportProgressDialogProperty.removeListener(exportProgressListener)
    }
}
