package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.narration.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class NarrationFooterViewModel : ViewModel() {
    private val workbookDataStore by inject<WorkbookDataStore>()
    private val narrationViewViewModel: NarrationViewViewModel by inject()

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

    init {
        subscribe<StickyVerseChangedEvent<Chunk>> {
            it.data?.let { verse ->
                stickyVerseProperty.set(verse)
            } ?: run {
                stickyVerseProperty.set(null)
            }
        }

        recordStartProperty.bind(narrationViewViewModel.recordStartProperty)
        recordResumeProperty.bind(narrationViewViewModel.recordResumeProperty)
        isRecordingProperty.bind(narrationViewViewModel.isRecordingProperty)
        recordPauseProperty.bind(narrationViewViewModel.recordPauseProperty)
        isRecordingAgainProperty.bind(narrationViewViewModel.isRecordingAgainProperty)
    }

    fun onDock() {
        val chapter = workbookDataStore.activeChapterProperty.value
        chapter.getDraft().subscribe {
            chunks.add(it)
        }
    }

    fun onRecord() {

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
                    isRecording -> messages["pauseRecording"]
                    isRecordingAgain -> messages["stopRecording"]
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

class NarrationFooter : View() {

    private val viewModel: NarrationFooterViewModel by inject()
    private var listView: NarrationTextListView<Chunk> by singleAssign()

    override fun onDock() {
        super.onDock()
        viewModel.onDock()

        listView.addListeners()

        /*subscribe<WaveformClickedEvent> {
            listView.apply {
                selectionModel.select(it.index)
                scrollTo(it.index)
            }
        }*/

        subscribe<ResumeVerseEvent> {
            viewModel.stickyVerseProperty.value?.let { verse ->
                listView.scrollTo(verse)
            }
        }

        subscribe<InitialSelectedVerseChangedEvent> {
            listView.apply {
                selectionModel.select(it.data)
                scrollTo(it.data)
            }
        }

        subscribe<RecordVerseEvent> {
            viewModel.onRecord()
        }

        subscribe<RecordAgainEvent> {
            listView.apply {
                selectionModel.select(it.index)
                scrollTo(it.index - 1)
            }
        }
    }

    override fun onUndock() {
        super.onUndock()

        listView.removeListeners()

        //TODO: Verify that unsubscribe works
        //unsubscribe<WaveformClickedEvent> {}
        unsubscribe<ResumeVerseEvent> {}
        unsubscribe<ResumeVerseEvent> {}
        unsubscribe<RecordVerseEvent> {}
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

class InitialSelectedVerseChangedEvent(val data: Chunk) : FXEvent()
