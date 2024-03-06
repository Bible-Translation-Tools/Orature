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

import javafx.animation.ParallelTransition
import javafx.animation.ScaleTransition
import javafx.animation.TranslateTransition
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.IdleEmptyState
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationState
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.VerseItemState
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.narration.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextItemData
import tornadofx.*
import java.text.MessageFormat
import kotlin.math.max

object RefreshTeleprompter : FXEvent()
class TeleprompterSeekEvent(val index: Int) : FXEvent()

class TeleprompterViewModel : ViewModel() {
    private val narrationViewModel: NarrationViewModel by inject()

    val chunks = narrationViewModel.narratableList
    var currentNarrationState = SimpleObjectProperty<NarrationState>()

    val stickyVerseProperty = SimpleObjectProperty<NarrationTextItemData>()
    val showStickyVerseProperty = SimpleBooleanProperty(false)

    private val recordStartProperty = SimpleBooleanProperty()
    private var recordStart by recordStartProperty

    private val recordPauseProperty = SimpleBooleanProperty()
    private var recordPause by recordPauseProperty

    private val recordResumeProperty = SimpleBooleanProperty()
    private var recordResume by recordResumeProperty

    val isRecordingProperty = SimpleBooleanProperty()
    private var isRecording by isRecordingProperty

    val isPlayingProperty = SimpleBooleanProperty()
    private var isPlaying by isPlayingProperty

    val isRecordingAgainProperty = SimpleBooleanProperty()
    private var isRecordingAgain by isRecordingAgainProperty


    val lastRecordedVerseProperty = SimpleIntegerProperty(0)

    val recordingVerseProperty = SimpleIntegerProperty()
    val playingVerseProperty = SimpleIntegerProperty()
    val highlightedVerseProperty = SimpleIntegerProperty()

    init {
        recordStartProperty.bindBidirectional(narrationViewModel.recordStartProperty)
        recordResumeProperty.bindBidirectional(narrationViewModel.recordResumeProperty)
        isRecordingProperty.bindBidirectional(narrationViewModel.isRecordingProperty)
        isPlayingProperty.bind(narrationViewModel.isPlayingProperty)
        recordPauseProperty.bindBidirectional(narrationViewModel.recordPauseProperty)
        isRecordingAgainProperty.bindBidirectional(narrationViewModel.isRecordingAgainProperty)
        lastRecordedVerseProperty.bindBidirectional(narrationViewModel.lastRecordedVerseProperty)
        recordingVerseProperty.bind(narrationViewModel.recordingVerseIndex)
        playingVerseProperty.bind(narrationViewModel.playingVerseIndex)
        highlightedVerseProperty.bind(narrationViewModel.highlightedVerseIndex)
        currentNarrationState.bind(narrationViewModel.narrationState)
    }

    fun currentVerseTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                stickyVerseProperty.value?.let { itemData ->
                    if (itemData.chunk.label == "verse") {
                        MessageFormat.format(
                            messages["currentVerseTitle"],
                            messages["verse"],
                            itemData.chunk.title
                        )
                    } else {
                        MessageFormat.format(
                            messages["currentTitle"],
                            itemData.chunk.textItem.text
                        )
                    }
                }
            },
            stickyVerseProperty
        )
    }

    fun recordButtonTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                when {
                    isRecording && !isRecordingAgain -> messages["pauseRecording"]
                    isRecording && isRecordingAgain -> messages["stopRecording"]
                    recordResume || recordPause -> messages["resumeRecording"]
                    else -> messages["beginRecording"]
                }
            },
            recordStartProperty,
            recordResumeProperty,
            isRecordingProperty,
            recordPauseProperty,
            isRecordingAgainProperty
        )
    }

    fun updateStickyVerse() {
        val activeStates = listOf(
            VerseItemState.RECORD_ACTIVE,
            VerseItemState.RECORD_AGAIN_ACTIVE,
            VerseItemState.RECORDING_PAUSED,
            VerseItemState.RECORD_AGAIN_PAUSED
        )
        val verse = narrationViewModel.narratableList
            .firstOrNull {
                it.verseState in activeStates || !it.hasRecording
            }

        stickyVerseProperty.set(verse)
    }
}

class TeleprompterView : View() {

    private val logger = LoggerFactory.getLogger(TeleprompterView::class.java)

    private val viewModel: TeleprompterViewModel by inject()
    private var listView: NarrationTextListView<NarrationTextItemData> by singleAssign()

    private val subscriptions = mutableListOf<EventRegistration>()

    init {
        subscribe<TeleprompterSeekEvent> {
            try {
                logger.info("Scrolling to ${it.index} for TeleprompterSeekEvent")
                runLater {
                    listView.scrollTo(it.index - 1)
                }
            } catch (e: Exception) {
                logger.error("Error scrolling to a Teleprompter item", e)
            }
        }

        subscribe<RefreshTeleprompter> {
            listView.refresh()
            viewModel.updateStickyVerse()
        }

        subscribe<StickyVerseChangedEvent> {
            viewModel.showStickyVerseProperty.set(it.showBanner)
        }

        subscribe<ResumeVerseEvent> {
            viewModel.stickyVerseProperty.value?.let { verse ->
                val index = listView.items.indexOfFirst { it == verse }
                try {
                    logger.info("Scrolling to $index for ResumeVerseEvent")
                    listView.scrollTo(max(0, index - 1)) // scrolls to item above the target for visual offset
                } catch (e: Exception) {
                    logger.error("Error scrolling to a Teleprompter item", e)
                }
            }
            viewModel.showStickyVerseProperty.set(false)
        }

        subscribe<RecordAgainEvent> {
            listView.apply {
                try {
                    logger.info("Scrolling to index ${it.index} for RecordAgainEvent")
                    scrollTo(it.index - 1)
                } catch (e: Exception) {
                    logger.error("Error scrolling to a Teleprompter item", e)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        listView.addListeners()
    }

    override fun onUndock() {
        super.onUndock()
        listView.removeListeners()
        subscriptions.forEach { it.unsubscribe() }
        subscriptions.clear()
    }

    override val root = vbox {
        addClass("narration__verses")
        vgrow = Priority.ALWAYS

        stickyVerse {
            verseLabelProperty.bind(viewModel.currentVerseTextBinding())
            resumeTextProperty.set(messages["resume"])

            visibleWhen { viewModel.showStickyVerseProperty.and(viewModel.stickyVerseProperty.isNotNull) }
            managedWhen(visibleProperty())

            visibleProperty().onChange {
                animateStickyVerse(it)
            }
        }

        narrationTextListview(viewModel.chunks) {
            addClass("narration__list")
            vgrow = Priority.ALWAYS

            listView = this
            firstVerseToResumeProperty.bind(viewModel.stickyVerseProperty)

            setCellFactory {
                NarrationTextCell(
                    messages["nextVerse"],
                    viewModel.recordButtonTextBinding(),
                    viewModel.currentNarrationState,
                    viewModel.highlightedVerseProperty,
                )
            }

            runLater { customizeScrollbarSkin() }

            viewModel.highlightedVerseProperty.onChange {
                if (it in items.indices) scrollTo(it)
            }
        }
    }

    private fun StickyVerse.animateStickyVerse(showing: Boolean) {
        if (showing) {
            opacity = 1.0
            val scaleTransition = ScaleTransition(Duration.seconds(0.6), this).apply {
                fromY = 0.2
                toY = 1.0
            }
            val tt1 = TranslateTransition(Duration.seconds(0.6), this).apply {
                fromY = -maxHeight / 2
                toY = 0.0
            }
            val animation = ParallelTransition(scaleTransition, tt1)
            animation.play()
        } else {
            opacity = 0.0
        }
    }
}
