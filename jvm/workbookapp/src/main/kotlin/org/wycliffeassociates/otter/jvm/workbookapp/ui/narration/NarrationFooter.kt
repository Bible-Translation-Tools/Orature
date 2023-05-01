package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ListView
import org.wycliffeassociates.otter.jvm.controls.narration.ResumeVerseEvent
import org.wycliffeassociates.otter.jvm.controls.narration.StickyVerseChangedEvent
import org.wycliffeassociates.otter.jvm.controls.narration.stickyVerse
import org.wycliffeassociates.otter.jvm.controls.narration.narrationTextListview
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.WaveformClickedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*
import java.text.MessageFormat

class NarrationFooterViewModel : ViewModel() {
    val allSortedChunks = observableListOf<ChunkData>()

    val stickyVerseProperty = SimpleObjectProperty<ChunkData>()

    init {
        subscribe<StickyVerseChangedEvent<ChunkData>> {
            it.data?.let { verse ->
                stickyVerseProperty.set(verse)
            } ?: run {
                stickyVerseProperty.set(null)
            }
        }
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
}

class NarrationFooter : View() {

    private val viewModel: NarrationFooterViewModel by inject()
    private var listView: ListView<ChunkData> by singleAssign()

    init {
        subscribe<WaveformClickedEvent> {
            listView.apply {
                selectionModel.select(it.data)
                scrollTo(it.data)
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

    override val root = stackpane {
        addClass("narration__verses")

        narrationTextListview(viewModel.allSortedChunks) {
            addClass("narration__list")

            listView = this

            setCellFactory {
                NarrationTextCell(messages["nextVerse"])
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

class InitialSelectedVerseChangedEvent(val data: ChunkData) : FXEvent()
