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

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.value.ChangeListener
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import java.util.*

class ChapterPage : Fragment() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: ChapterPageViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val navigator: NavigationMediator by inject()
    private lateinit var chunkListView: ListView<CardData>

    private val pluginOpenedPage: PluginOpenedPage
    private var exportProgressListener: ChangeListener<Boolean>? = null

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(viewModel.breadcrumbTitleBinding(this@ChapterPage))
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK))
        onClickAction {
            navigator.dock(this@ChapterPage)
        }
    }

    override fun onDock() {
        super.onDock()
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)

        viewModel.setWorkChunk()
        viewModel.openPlayers()

        viewModel.checkCanCompile()
        chunkListView.refresh()

        initializeProgressDialog()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.closePlayers()
        removeDialogListeners()
    }

    init {
        importStylesheet(resources.get("/css/chapter-page.css"))
        importStylesheet(resources.get("/css/chunk-item.css"))
        importStylesheet(resources.get("/css/take-item.css"))
        importStylesheet(resources.get("/css/add-plugin-dialog.css"))
        importStylesheet(resources.get("/css/confirm-dialog.css"))

        pluginOpenedPage = createPluginOpenedPage()
        workspace.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
                viewModel.openSourceAudioPlayer()
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
            viewModel.openPlayers()
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
                            FX.messages["chapter"].capitalize(),
                            it?.bodyText
                        )
                    })
                }

                hbox {
                    addClass("chapter-page__chapter-audio")
                    vgrow = Priority.ALWAYS

                    simpleaudioplayer {
                        hgrow = Priority.ALWAYS
                        playerProperty.bind(viewModel.chapterPlayerProperty)
                        visibleWhen(playerProperty.isNotNull)
                        managedProperty().bind(visibleProperty())
                    }

                    hbox {
                        hgrow = Priority.ALWAYS
                        addClass("chapter-page__not-started")

                        label(messages["draftingNotStarted"])

                        visibleWhen(viewModel.chapterPlayerProperty.isNull)
                        managedProperty().bind(visibleProperty())
                    }
                }

                hbox {
                    addClass("chapter-page__chapter-actions")
                    button {
                        addClass("btn", "btn--secondary")
                        text = messages["recordChapter"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        action {
                            viewModel.recordChapter()
                        }
                    }
                    button {
                        addClass("btn", "btn--secondary")
                        text = messages["exportChapter"]
                        graphic = FontIcon(Material.UPLOAD_FILE)
                        action {
                            viewModel.exportChapter()
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
                    graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                    enableWhen(viewModel.selectedChapterTakeProperty.isNotNull)
                    action {
                        viewModel.processTakeWithPlugin(PluginType.EDITOR)
                    }
                }
                button {
                    addClass("btn", "btn--primary")
                    text = messages["markAction"]
                    graphic = FontIcon(MaterialDesign.MDI_BOOKMARK)
                    enableWhen(viewModel.selectedChapterTakeProperty.isNotNull)
                    action {
                        viewModel.processTakeWithPlugin(PluginType.MARKER)
                    }
                }
                button {
                    addClass("btn", "btn--primary")
                    text = messages["viewTakes"]
                    graphic = FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC)
                    enableWhen(viewModel.selectedChapterTakeProperty.isNotNull)
                    action {
                        viewModel.chapterCardProperty.value?.let {
                            viewModel.onCardSelection(it)
                            navigator.dock<RecordScripturePage>()
                        }
                    }
                }
            }
        }

        vbox {
            addClass("chapter-page__chunks")
            vgrow = Priority.ALWAYS

            hbox {
                addClass("chapter-page__chunks-header")
                button {
                    addClass("btn", "btn--secondary", "btn--secondary-light")
                    text = messages["compile"]
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
                chunkListView = this
                fitToParentHeight()
                setCellFactory {
                    ChunkCell(::getPlayer, ::onChunkOpen, ::onTakeSelected)
                }
            }
        }
    }

    private fun onChunkOpen(chunk: CardData) {
        viewModel.onCardSelection(chunk)
        navigator.dock<RecordScripturePage>()
    }

    private fun onTakeSelected(chunk: CardData, take: TakeModel) {
        chunk.chunkSource?.audio?.selectTake(take.take)
        workbookDataStore.updateSelectedTakesFile()
        take.take.file.setLastModified(System.currentTimeMillis())
    }

    private fun getPlayer(): IAudioPlayer {
        return (app as OtterApp).dependencyGraph.injectPlayer()
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return PluginOpenedPage().apply {
            dialogTitleProperty.bind(viewModel.dialogTitleBinding())
            dialogTextProperty.bind(viewModel.dialogTextBinding())
            playerProperty.bind(viewModel.sourceAudioPlayerProperty)
            audioAvailableProperty.bind(viewModel.sourceAudioAvailableProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
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
        }
    }

    private fun removeDialogListeners() {
        viewModel.showExportProgressDialogProperty.removeListener(exportProgressListener)
    }

}
