package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.controls.chapterSelector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.narrationMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class NarrationHeader : View() {
    private val viewModel by inject<NarrationHeaderViewModel>()

    override val root = hbox {
        addClass("narration__header")

        hbox {
            narrationTitle(viewModel.titleProperty)
            hgrow = Priority.SOMETIMES
        }
        hbox {
            addClass("narration__header-controls")
            narrationMenu {
                hasUndoProperty.bind(viewModel.hasUndoProperty)
                hasRedoProperty.bind(viewModel.hasRedoProperty)
                hasChapterFileProperty.bind(viewModel.hasChapterFileProperty)
            }
            chapterSelector {
                chapterTitleProperty.bind(viewModel.chapterTitleProperty)

                prevDisabledProperty.bind(viewModel.hasPreviousChapter.not())
                nextDisabledProperty.bind(viewModel.hasNextChapter.not())

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
    private val workbookDataStore by inject<WorkbookDataStore>()
    private val narrationViewViewModel: NarrationViewViewModel by inject()

    // val titleProperty = SimpleStringProperty("Narration Title")
    // val chapterTitleProperty = SimpleStringProperty("Chapter Title")

    val titleProperty = workbookDataStore.activeWorkbookProperty.stringBinding {
        it?.let {
            MessageFormat.format(
                // messages["narrationTitle"],
                it.target.title
            )
        } ?: ""
    }

    val chapterTitleProperty = workbookDataStore.activeChapterProperty.stringBinding {
        it?.let {
            MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                it.title
            )
        } ?: ""
    }

    val hasNextChapter = SimpleBooleanProperty()
    val hasPreviousChapter = SimpleBooleanProperty()
    val hasChapterFileProperty = SimpleBooleanProperty()

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()

    init {
        hasUndoProperty.bind(narrationViewViewModel.hasUndoProperty)
        hasRedoProperty.bind(narrationViewViewModel.hasRedoProperty)
    }

    fun selectPreviousChapter() {
        TODO("Not yet implemented")
    }

    fun selectNextChapter() {
        TODO("Not yet implemented")
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