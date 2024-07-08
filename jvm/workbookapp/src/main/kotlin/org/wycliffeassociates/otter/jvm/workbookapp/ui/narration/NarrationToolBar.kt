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
import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import java.util.*

class NarrationToolBar : View() {
    private val viewModel: NarrationToolbarViewModel by inject()

    override val root = hbox {
        addClass("narration-toolbar", "narration-toolbar__play-controls")
        alignment = Pos.CENTER_LEFT
        button {
            addClass("btn", "btn--secondary")
            tooltip { textProperty().bind(this@button.textProperty()) }

            disableWhen {
                viewModel.isPlayPauseButtonDisabled
            }

            viewModel.narrationStateProperty.onChangeAndDoNow {
                it?.let { state ->
                    runLater {
                        if (state == NarrationStateType.PLAYING) {
                            graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                            text = messages["pause"]
                            addPseudoClass("active")
                        } else {
                            graphic = FontIcon(MaterialDesign.MDI_PLAY)
                            text = messages["playAll"]
                            removePseudoClass("active")
                        }
                    }
                }
            }

            setOnAction {
                if (viewModel.narrationStateProperty.value == NarrationStateType.PLAYING) {
                    viewModel.pausePlayback()
                } else {
                    viewModel.playAll()
                }
            }
        }
        button {
            addClass("btn", "btn--tertiary")
            tooltip(messages["previousVerse"])
            graphic = FontIcon(MaterialDesign.MDI_SKIP_PREVIOUS)
            setOnAction {
                viewModel.seekToPrevious()
            }
            disableWhen {
                viewModel.isVerseNavigationDisabled
            }
        }
        button {
            addClass("btn", "btn--tertiary")
            tooltip(messages["nextVerse"])
            graphic = FontIcon(MaterialDesign.MDI_SKIP_NEXT)
            setOnAction {
                viewModel.seekToNext()
            }
            disableWhen {
                viewModel.isVerseNavigationDisabled
            }
        }
    }
}

class NarrationToolbarViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(NarrationHeaderViewModel::class.java)

    private val workbookDataStore by inject<WorkbookDataStore>()
    private val narrationViewModel: NarrationViewModel by inject()

    val titleProperty = workbookDataStore.activeWorkbookProperty.stringBinding {
        it?.let {
            val title = it.target.title.takeIf { bookTitle -> bookTitle.isNotEmpty() }
                ?: it.target.slug.uppercase(Locale.getDefault())

            MessageFormat.format(
                messages["narrationTitle"],
                title
            )
        } ?: ""
    }

    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()

    val isVerseNavigationDisabled = SimpleBooleanProperty()
    val isPlayPauseButtonDisabled = SimpleBooleanProperty()

    private val chapterList: ObservableList<Chapter> = observableListOf()

    init {
        chapterList.bind(narrationViewModel.chapterList) { it }
        narrationStateProperty.bind(narrationViewModel.narrationStateProperty)

        isVerseNavigationDisabled.bind(
            Bindings.createBooleanBinding(
                {
                    narrationViewModel.narrationStateProperty.value?.let {
                        it == NarrationStateType.RECORDING
                                || it == NarrationStateType.RECORDING_AGAIN
                                || it == NarrationStateType.NOT_STARTED
                                || it == NarrationStateType.RECORDING_AGAIN_PAUSED
                                || it == NarrationStateType.PLAYING
                    } ?: false
                },
                narrationViewModel.narrationStateProperty
            )
        )

        isPlayPauseButtonDisabled.bind(
            Bindings.createBooleanBinding(
                {
                    narrationViewModel.narrationStateProperty.value?.let {
                        it == NarrationStateType.RECORDING
                                || it == NarrationStateType.RECORDING_AGAIN
                                || it == NarrationStateType.NOT_STARTED
                                || it == NarrationStateType.RECORDING_PAUSED
                                || it == NarrationStateType.RECORDING_AGAIN_PAUSED
                    } ?: false
                },
                narrationViewModel.narrationStateProperty
            )
        )


    }


    fun pausePlayback() {
        narrationViewModel.pausePlayback()
    }

    fun seekToNext() {
        narrationViewModel.seekToNext()
    }

    fun seekToPrevious() {
        narrationViewModel.seekToPrevious()
    }

    fun playAll() {
        narrationViewModel.playAll()
    }
}