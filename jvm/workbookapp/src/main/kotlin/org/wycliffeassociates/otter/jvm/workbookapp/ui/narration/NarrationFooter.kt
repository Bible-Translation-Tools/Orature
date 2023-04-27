package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.jvm.controls.narration.stickyVerse
import org.wycliffeassociates.otter.jvm.controls.narration.narrationTextListview
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*
import java.text.MessageFormat

class NarrationFooterViewModel : ViewModel() {
    val allSortedChunks = observableListOf<ChunkData>()

    val stickyVerseVisibleProperty = SimpleBooleanProperty()
    val currentVerseLabelProperty = SimpleStringProperty()

    fun currentVerseTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                val title = messages["currentVerseTitle"]
                val verseTitle = messages["verse"]
                val stickyVerseLabel = currentVerseLabelProperty.value

                if (title != null && verseTitle != null && stickyVerseLabel != null) {
                    MessageFormat.format(
                        title,
                        verseTitle,
                        stickyVerseLabel
                    )
                } else {
                    ""
                }
            },
            currentVerseLabelProperty
        )
    }
}

class NarrationFooter : View() {

    private val viewModel: NarrationFooterViewModel by inject()

    override val root = stackpane {
        addClass("narration__verses")

        narrationTextListview(viewModel.allSortedChunks) {
            addClass("narration__list")

            //viewModel.onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)
            //viewModel.floatingCardVisibleProperty.bind(cardIsOutOfViewProperty)

            //initialSelectedItemProperty.bind(viewModel.initialSelectedItemProperty)
            /*viewModel.currentVerseLabelProperty.bind(selectionModel.selectedItemProperty().stringBinding {
                it?.title
            })*/

            /*viewModel.onScrollToChunk = {
                selectionModel.select(it)
                scrollTo(it)
            }*/

            setCellFactory {
                NarrationTextCell(messages["nextVerse"])
            }
        }

        stickyVerse {
            verseLabelProperty.bind(viewModel.currentVerseTextBinding())
            resumeTextProperty.set(messages["resume"])

            visibleWhen {
                viewModel.stickyVerseVisibleProperty
            }

            onResumeVerse {
                fire(ResumeVerse())
            }

        }
    }
}

class ResumeVerse: FXEvent()