package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.narration.*
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.WaveformClickedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class NarrationFooterViewModel : ViewModel() {
    val workbookDataStore by inject<WorkbookDataStore>()

    val allSortedChunks = observableListOf<Chunk>()

    val stickyVerseProperty = SimpleObjectProperty<Chunk>()

    var recordStartedProperty = SimpleBooleanProperty(false)
    var recordStarted by recordStartedProperty

    var recordPausedProperty = SimpleBooleanProperty(false)
    var recordPaused by recordPausedProperty

    // TODO: Should be only chunks that have been recorded
    val recordedChunks = FilteredList(allSortedChunks)

    init {
        subscribe<StickyVerseChangedEvent<Chunk>> {
            it.data?.let { verse ->
                stickyVerseProperty.set(verse)
            } ?: run {
                stickyVerseProperty.set(null)
            }
        }
    }

    fun onDock() {
        val chapter = workbookDataStore.activeChapterProperty.value
        chapter.getDraft().subscribe {
            allSortedChunks.add(it)
        }
        println(allSortedChunks)
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
                    recordStarted && !recordPaused -> messages["pauseRecording"]
                    recordedChunks.isNotEmpty() || recordPaused -> messages["resumeRecording"]
                    else -> messages["beginRecording"]
                }
            },
            recordStartedProperty,
            recordPausedProperty,
            recordedChunks
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

        subscribe<WaveformClickedEvent> {
            listView.apply {
                selectionModel.select(it.index)
                scrollTo(it.index)
            }
        }

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
    }

    override fun onUndock() {
        super.onUndock()

        listView.removeListeners()

        //TODO: Verify that unsubscribe works
        unsubscribe<WaveformClickedEvent> { }
        unsubscribe<ResumeVerseEvent> {  }
        unsubscribe<ResumeVerseEvent> {  }
    }

    override val root = stackpane {
        addClass("narration__verses")

        narrationTextListview(viewModel.allSortedChunks) {
            addClass("narration__list")

            listView = this

            setCellFactory {
                NarrationTextCell(
                    messages["nextVerse"],
                    viewModel.recordButtonTextBinding(),
                    viewModel.recordPausedProperty.not()
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
