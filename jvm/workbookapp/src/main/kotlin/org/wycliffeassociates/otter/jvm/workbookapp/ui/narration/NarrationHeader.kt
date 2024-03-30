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

import javafx.beans.binding.Bindings
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
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType
import org.wycliffeassociates.otter.common.domain.project.ProjectCompletionStatus
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.chapterselector.chapterSelector
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
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
                enableWhen(viewModel.isUndoEnabledButtonProperty)
            }
            button {
                tooltip = tooltip(messages["redoAction"])
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_REDO)
                setOnAction {
                    FX.eventbus.fire(NarrationRedoEvent())
                }
                enableWhen(viewModel.isRedoEnabledButtonProperty)
            }
            narrationMenuButton(
                viewModel.narrationStateProperty
            ) {
                disableWhen(viewModel.isChapterMenuButtonDisabled)
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

                prevDisabledProperty.bind(viewModel.isPreviousChapterButtonDisabled)
                nextDisabledProperty.bind(viewModel.isNextChapterButtonDisabled)

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

    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()
    val isUndoEnabledButtonProperty = SimpleBooleanProperty()
    val isRedoEnabledButtonProperty = SimpleBooleanProperty()
    val isChapterMenuButtonDisabled = SimpleBooleanProperty()
    val isNextChapterButtonDisabled = SimpleBooleanProperty()
    val isPreviousChapterButtonDisabled = SimpleBooleanProperty()

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        chapterTitleProperty.bind(narrationViewModel.chapterTitleProperty)
        narrationStateProperty.bind(narrationViewModel.narrationStateProperty)

        isUndoEnabledButtonProperty.bind(
            Bindings.createBooleanBinding(
                {
                    val isRecordingAgainPaused =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN_PAUSED

                    val isRecording =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN
                                || narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING

                    narrationViewModel.hasUndoProperty.value && !(isRecording || isRecordingAgainPaused)
                },
                narrationViewModel.hasUndoProperty, narrationViewModel.narrationStateProperty
            )
        )

        isRedoEnabledButtonProperty.bind(
            Bindings.createBooleanBinding(
                {
                    val isRecordingAgainPaused =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN_PAUSED

                    val isRecording =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN
                                || narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING

                    narrationViewModel.hasRedoProperty.value && !(isRecording || isRecordingAgainPaused)
                },
                narrationViewModel.hasRedoProperty, narrationViewModel.narrationStateProperty
            )
        )

        isChapterMenuButtonDisabled.bind(
            Bindings.createBooleanBinding(
                {
                    val isRecordingAgainPaused =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN_PAUSED

                    val isRecording =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN
                                || narrationViewModel.narrationStateProperty.value == NarrationStateType.RECORDING

                    val isModifyingAudio =
                        narrationViewModel.narrationStateProperty.value == NarrationStateType.MODIFYING_AUDIO_FILE

                    isRecordingAgainPaused || isRecording || isModifyingAudio
                },
                narrationViewModel.narrationStateProperty
            )
        )


        isNextChapterButtonDisabled.bind(
            Bindings.createBooleanBinding(
                {
                    val isRecording = narrationViewModel.narrationStateProperty.value?.let {
                        it == NarrationStateType.RECORDING || it == NarrationStateType.RECORDING_AGAIN
                    } ?: false

                    narrationViewModel.hasNextChapter.value.not() || isRecording
                },
                narrationViewModel.hasNextChapter, narrationViewModel.narrationStateProperty
            )
        )

        isPreviousChapterButtonDisabled.bind(
            Bindings.createBooleanBinding(
                {
                    val isRecording = narrationViewModel.narrationStateProperty.value?.let {
                        it == NarrationStateType.RECORDING || it == NarrationStateType.RECORDING_AGAIN
                    } ?: false

                    narrationViewModel.hasPreviousChapter.value.not() || isRecording
                },
                narrationViewModel.hasPreviousChapter, narrationViewModel.narrationStateProperty
            )
        )
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
        narrationViewModel.processWithPlugin(pluginType)
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