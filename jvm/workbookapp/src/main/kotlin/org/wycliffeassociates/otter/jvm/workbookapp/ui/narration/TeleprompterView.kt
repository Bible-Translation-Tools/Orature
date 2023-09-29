package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.util.Duration
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.narration.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextItemData
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.TimeUnit
import kotlin.math.max

object RefreshTeleprompter : FXEvent()

class TeleprompterViewModel : ViewModel() {
    private val narrationViewModel: NarrationViewModel by inject()

    val chunks = observableListOf<NarrationTextItemData>()

    val stickyVerseProperty = SimpleObjectProperty<Chunk>()

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

    init {
        chunks.bind(narrationViewModel.narratableList) {
            it
        }

        recordStartProperty.bindBidirectional(narrationViewModel.recordStartProperty)
        recordResumeProperty.bindBidirectional(narrationViewModel.recordResumeProperty)
        isRecordingProperty.bindBidirectional(narrationViewModel.isRecordingProperty)
        isPlayingProperty.bind(narrationViewModel.isPlayingProperty)
        recordPauseProperty.bindBidirectional(narrationViewModel.recordPauseProperty)
        isRecordingAgainProperty.bindBidirectional(narrationViewModel.isRecordingAgainProperty)
        lastRecordedVerseProperty.bindBidirectional(narrationViewModel.lastRecordedVerseProperty)
        recordingVerseProperty.bind(narrationViewModel.recordingVerseIndex)
        playingVerseProperty.bind(narrationViewModel.playingVerseIndex)
    }

    fun currentVerseTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                val title = messages["currentVerseTitle"]
                val verseTitle = messages["verse"]
                val stickyVerseLabel = stickyVerseProperty.value?.title

                MessageFormat.format(
                    title,
                    verseTitle,
                    stickyVerseLabel
                )
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
}

class TeleprompterView : View() {

    private val viewModel: TeleprompterViewModel by inject()
    private var listView: NarrationTextListView<NarrationTextItemData> by singleAssign()

    private val subscriptions = mutableListOf<EventRegistration>()

    init {
        subscribe<RefreshTeleprompter> {
            listView.refresh()
        }

        subscribe<StickyVerseChangedEvent<NarrationTextItemData>> {
            it.data?.let { narrationItem ->
                viewModel.stickyVerseProperty.set(narrationItem.chunk)
            } ?: run {
                viewModel.stickyVerseProperty.set(null)
            }
        }.let { subscriptions.add(it) }

        subscribe<ResumeVerseEvent> {
            viewModel.stickyVerseProperty.value?.let { verse ->
                val item = listView.items.find { it.chunk == verse }
                listView.scrollTo(item)
            }
        }.let { subscriptions.add(it) }

        subscribe<RecordAgainEvent> {
            listView.apply {
                selectionModel.select(it.index)
                scrollTo(it.index - 1)
            }
        }.let { subscriptions.add(it) }
    }

    override fun onDock() {
        super.onDock()
        listView.addListeners()

        viewModel.lastRecordedVerseProperty.value?.let { lastVerse ->
            listView.apply {
                runLater(Duration.millis(1000.0)) {
                    val index = lastVerse.coerceIn(0, max(viewModel.chunks.size - 1, 0))
                    selectionModel.select(index)
                    scrollTo(index)
                }
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        listView.removeListeners()
        subscriptions.forEach { it.unsubscribe() }
        subscriptions.clear()
    }

    override val root = stackpane {
        addClass("narration__verses")

        narrationTextListview(viewModel.chunks) {
            addClass("narration__list")

            listView = this

            setCellFactory {
                NarrationTextCell(
                    messages["nextVerse"],
                    viewModel.recordButtonTextBinding(),
                    viewModel.isRecordingProperty,
                    viewModel.isRecordingAgainProperty,
                    viewModel.isPlayingProperty,
                    viewModel.recordingVerseProperty,
                    viewModel.playingVerseProperty
                )
            }

            runLater { customizeScrollbarSkin() }
        }

        stickyVerse {
            verseLabelProperty.bind(viewModel.currentVerseTextBinding())
            resumeTextProperty.set(messages["resume"])

            visibleWhen {
                viewModel.stickyVerseProperty.isNotNull
            }
        }
    }
}
