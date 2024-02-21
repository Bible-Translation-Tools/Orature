/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.project.ProjectCompletionStatus
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.chapterselector.chapterSelector
import org.wycliffeassociates.otter.jvm.controls.event.ChapterReturnFromPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.ChapterSelectorPopup
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationOpenInPluginEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.narrationMenuButton
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import javax.inject.Inject

class NarrationHeader : View() {
    private val viewModel by inject<NarrationHeaderViewModel>()

    private val popupMenu = ChapterSelectorPopup()

    init {
        subscribe<NarrationOpenInPluginEvent> {
            viewModel.processWithPlugin(it.plugin)
        }

        subscribe<NavigateChapterEvent> {
            popupMenu.hide()
        }
    }

    override val root = hbox {

        addClass("narration__header")

        hbox {
            narrationTitle(viewModel.titleProperty)
            hgrow = Priority.SOMETIMES
        }
        hbox {
            addClass("narration__header-controls")
            button {
                tooltip = tooltip(messages["undoAction"])
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_UNDO)
                setOnAction {
                    FX.eventbus.fire(NarrationUndoEvent())
                }
                enableWhen(viewModel.hasUndoProperty.and(viewModel.isRecordingProperty.not()))
            }
            button {
                tooltip = tooltip(messages["redoAction"])
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_REDO)
                setOnAction {
                    FX.eventbus.fire(NarrationRedoEvent())
                }
                enableWhen(viewModel.hasRedoProperty.and(viewModel.isRecordingProperty.not()))
            }
            narrationMenuButton(
                viewModel.hasChapterTakeProperty,
                viewModel.hasVersesProperty,
                viewModel.hasAllItemsRecordedProperty
            ) {
                enableWhen(viewModel.chapterTakeBusyProperty.not().and(viewModel.isRecordingProperty.not()))
            }
            chapterSelector {
                chapterTitleProperty.bind(viewModel.chapterTitleProperty)

                setOnChapterSelectorOpenedProperty {

                    popupMenu.updateChapterGrid(viewModel.chapterList)

                    val bound = this.boundsInLocal
                    val screenBound = this.localToScreen(bound)

                    popupMenu.show(FX.primaryStage)

                    popupMenu.x = screenBound.minX - popupMenu.width + this.width
                    popupMenu.y = screenBound.maxY - 25
                }

                prevDisabledProperty.bind(viewModel.hasPreviousChapter.not().or(viewModel.isRecordingProperty))
                nextDisabledProperty.bind(viewModel.hasNextChapter.not().or(viewModel.isRecordingProperty))

                setOnPreviousChapter {
                    viewModel.selectPreviousChapter()
                }
                setOnNextChapter {
                    viewModel.selectNextChapter()
                }
            }
        }
    }
}

class NarrationHeaderViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(NarrationHeaderViewModel::class.java)

    @Inject
    lateinit var projectCompletionStatus: ProjectCompletionStatus

    private val workbookDataStore by inject<WorkbookDataStore>()
    private val narrationViewModel: NarrationViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val titleProperty = workbookDataStore.activeWorkbookProperty.stringBinding {
        it?.let {
            MessageFormat.format(
                messages["narrationTitle"],
                it.target.title
            )
        } ?: ""
    }

    val chapterTitleProperty = SimpleStringProperty()

    val hasNextChapter = SimpleBooleanProperty()
    val hasPreviousChapter = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()
    val hasAllItemsRecordedProperty = SimpleBooleanProperty()
    val chapterTakeProperty = SimpleObjectProperty<Take>()
    val hasChapterTakeProperty = chapterTakeProperty.isNotNull
    val chapterTakeBusyProperty = SimpleBooleanProperty()

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()
    val isRecordingProperty = narrationViewModel.isRecordingProperty.or(narrationViewModel.isRecordingAgainProperty)

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        chapterTakeProperty.bind(narrationViewModel.chapterTakeProperty)
        chapterTitleProperty.bind(narrationViewModel.chapterTitleProperty)
        hasNextChapter.bind(narrationViewModel.hasNextChapter)
        hasPreviousChapter.bind(narrationViewModel.hasPreviousChapter)
        chapterTakeBusyProperty.bind(narrationViewModel.isModifyingTakeAudioProperty)

        hasUndoProperty.bind(narrationViewModel.hasUndoProperty)
        hasRedoProperty.bind(narrationViewModel.hasRedoProperty)
        hasVersesProperty.bind(narrationViewModel.hasVersesProperty)
        hasAllItemsRecordedProperty.bind(narrationViewModel.hasAllItemsRecordedProperty)
    }

    val chapterList: List<ChapterGridItemData>
        get() {
            return narrationViewModel.chapterList.map { chapter ->
                val wb = workbookDataStore.workbook
                val chapterProgress: Double = if (hasInProgressNarration(wb, chapter)) {
                    projectCompletionStatus.getChapterNarrationProgress(wb, chapter)
                } else {
                    0.0
                }
                val isCompleted = chapterProgress == 1.0

                ChapterGridItemData(
                    chapter.sort,
                    isCompleted,
                    workbookDataStore.activeChapterProperty.value?.sort == chapter.sort
                )
            }
        }

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    fun selectPreviousChapter() {
        logger.info("Selecting previous chapter")
        stepToChapter(StepDirection.BACKWARD)
    }

    fun selectNextChapter() {
        logger.info("Selecting next chapter")
        stepToChapter(StepDirection.FORWARD)
    }

    private fun stepToChapter(direction: StepDirection) {
        val step = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        val nextIndex =
            narrationViewModel.chapterList.indexOf(workbookDataStore.chapter) + step

        narrationViewModel.chapterList
            .elementAtOrNull(nextIndex)
            ?.let {
                fire(NavigateChapterEvent(it.sort))
            }
    }


    fun processWithPlugin(pluginType: PluginType) {
        chapterTakeProperty.value?.let { take ->
            workbookDataStore.activeChapterProperty.value?.audio?.let { audio ->
                pluginContextProperty.set(pluginType)
                workbookDataStore.activeTakeNumberProperty.set(take.number)

                audioPluginViewModel
                    .getPlugin(pluginType)
                    .doOnError { e ->
                        logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
                    }
                    .flatMapSingle { plugin ->
                        narrationViewModel.pluginOpenedProperty.set(true)
                        fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                        when (pluginType) {
                            PluginType.EDITOR -> audioPluginViewModel.edit(audio, take)
                            PluginType.MARKER -> audioPluginViewModel.mark(audio, take)
                            else -> null
                        }
                    }
                    .observeOnFx()
                    .doOnError { e ->
                        logger.error("Error in processing take with plugin type: $pluginType - $e")
                    }
                    .onErrorReturn { PluginActions.Result.NO_PLUGIN }
                    .subscribe { result: PluginActions.Result ->
                        logger.info("Returned from plugin with result: $result")
                        FX.eventbus.fire(PluginClosedEvent(pluginType))

                        when (result) {
                            PluginActions.Result.NO_PLUGIN -> FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                            else -> {
                                when (pluginType) {
                                    PluginType.EDITOR, PluginType.MARKER -> {
                                        FX.eventbus.fire(ChapterReturnFromPluginEvent())
                                    }

                                    else -> {
                                        logger.error("Plugin returned with result $result, plugintype: $pluginType did not match a known plugin.")
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun hasInProgressNarration(workbook: Workbook, chapter: Chapter): Boolean {
        val files = workbook.projectFilesAccessor.getInProgressNarrationFiles(workbook, chapter)
        return files.all { it.exists() }
    }
}

class NarrationTitle(val titleTextProperty: ObservableValue<String> = SimpleStringProperty()) : HBox() {
    constructor(titleText: String) : this(SimpleStringProperty(titleText))

    init {
        addClass("narration__header")
        label(titleTextProperty) {
            addClass("narration__header-title")
        }
    }
}

fun EventTarget.narrationTitle(
    titleTextProperty: ObservableValue<String>, op: NarrationTitle.() -> Unit = {}
) = NarrationTitle(titleTextProperty).attachTo(this, op)

fun EventTarget.narrationTitle(
    titleText: String, op: NarrationTitle.() -> Unit = {}
) = NarrationTitle(titleText).attachTo(this, op)