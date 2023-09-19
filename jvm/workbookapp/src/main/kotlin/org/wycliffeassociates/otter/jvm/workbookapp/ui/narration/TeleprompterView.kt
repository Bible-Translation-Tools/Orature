package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.util.Duration
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.narration.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import kotlin.math.max

class TeleprompterViewModel : ViewModel() {
    private val narrationViewModel: NarrationViewModel by inject()

    val chunks = observableListOf<Chunk>()

    val stickyVerseProperty = SimpleObjectProperty<Chunk>()

    private val recordStartProperty = SimpleBooleanProperty()
    private var recordStart by recordStartProperty

    private val recordPauseProperty = SimpleBooleanProperty()
    private var recordPause by recordPauseProperty

    private val recordResumeProperty = SimpleBooleanProperty()
    private var recordResume by recordResumeProperty

    val isRecordingProperty = SimpleBooleanProperty()
    private var isRecording by isRecordingProperty

    val isRecordingAgainProperty = SimpleBooleanProperty()
    private var isRecordingAgain by isRecordingAgainProperty

    val lastRecordedVerseProperty = SimpleIntegerProperty()

    init {
        chunks.bind(narrationViewModel.chunksList) { it }

        recordStartProperty.bindBidirectional(narrationViewModel.recordStartProperty)
        recordResumeProperty.bindBidirectional(narrationViewModel.recordResumeProperty)
        isRecordingProperty.bindBidirectional(narrationViewModel.isRecordingProperty)
        recordPauseProperty.bindBidirectional(narrationViewModel.recordPauseProperty)
        isRecordingAgainProperty.bindBidirectional(narrationViewModel.isRecordingAgainProperty)
        lastRecordedVerseProperty.bindBidirectional(narrationViewModel.lastRecordedVerseProperty)
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
    private var listView: NarrationTextListView<Chunk> by singleAssign()

    private val subscriptions = mutableListOf<EventRegistration>()

    init {
        /*subscribe<WaveformClickedEvent> {
            listView.apply {
                selectionModel.select(it.index)
                scrollTo(it.index)
            }
        }*/

        subscribe<StickyVerseChangedEvent<Chunk>> {
            it.data?.let { verse ->
                viewModel.stickyVerseProperty.set(verse)
            } ?: run {
                viewModel.stickyVerseProperty.set(null)
            }
        }.let { subscriptions.add(it) }

        subscribe<ResumeVerseEvent> {
            viewModel.stickyVerseProperty.value?.let { verse ->
                listView.scrollTo(verse)
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
                    viewModel.isRecordingAgainProperty
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
