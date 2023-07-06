package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.controls.chapterSelector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.narrationMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
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
                hasVersesProperty.bind(viewModel.hasVersesProperty)
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

    override fun onDock() {
        super.onDock()
        viewModel.onDock()
    }

    override fun onUndock() {
        super.onDock()
        viewModel.onUndock()
    }
}

class NarrationHeaderViewModel : ViewModel() {
    private val workbookDataStore by inject<WorkbookDataStore>()
    private val narrationViewViewModel: NarrationViewViewModel by inject()

    val titleProperty = workbookDataStore.activeWorkbookProperty.stringBinding {
        it?.let {
            MessageFormat.format(
                messages["narrationTitle"],
                it.target.title
            )
        } ?: ""
    }

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    val chapterTitleProperty = SimpleStringProperty()

    val hasNextChapter = SimpleBooleanProperty()
    val hasPreviousChapter = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()

    val chapterFileProperty = SimpleObjectProperty<File>()
    val hasChapterFileProperty = chapterFileProperty.isNotNull

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()

    private val chapterList: ObservableList<Chapter> = observableListOf()

    val listeners = mutableListOf<ListenerDisposer>()

    init {
        hasUndoProperty.bind(narrationViewViewModel.hasUndoProperty)
        hasRedoProperty.bind(narrationViewViewModel.hasRedoProperty)
        hasVersesProperty.bind(narrationViewViewModel.hasVersesProperty)
    }

    fun onDock() {
        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer {
            it?.let { chapter ->
                setHasNextAndPreviousChapter(chapter)
                loadChapter(chapter)
            }
        }.let(listeners::add)

        workbookDataStore.activeWorkbookProperty.onChangeAndDoNowWithDisposer { workbook ->
            workbook?.let {
                getChapterList(workbook.target.chapters)
            }
        }.let(listeners::add)
    }

    fun onUndock() {
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    fun selectPreviousChapter() {
        stepToChapter(StepDirection.BACKWARD)
    }

    fun selectNextChapter() {
        stepToChapter(StepDirection.FORWARD)
    }

    private fun loadChapter(chapter: Chapter) {
        chapterFileProperty.set(chapter.audio.selected.value?.value?.file)
        chapterTitleProperty.set(
            MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                chapter.title
            )
        )
    }

    private fun getChapterList(chapters: Observable<Chapter>) {
        chapters
            .toList()
            .map { it.sortedBy { chapter -> chapter.sort } }
            .observeOnFx()
            .doOnError { e ->
                //logger.error("Error in getting the chapter list", e)
            }
            .subscribe { list ->
                chapterList.setAll(list)
            }
    }

    private fun setHasNextAndPreviousChapter(chapter: Chapter) {
        if (chapterList.isNotEmpty()) {
            hasNextChapter.set(chapter.sort < chapterList.last().sort)
            hasPreviousChapter.set(chapter.sort > chapterList.first().sort)
        } else {
            hasNextChapter.set(false)
            hasPreviousChapter.set(false)
            chapterList.sizeProperty.onChangeOnce {
                setHasNextAndPreviousChapter(chapter)
            }
        }
    }

    private fun stepToChapter(direction: StepDirection) {
        val step = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        val nextIndex = chapterList.indexOf(workbookDataStore.chapter) + step
        chapterList.elementAtOrNull(nextIndex)?.let {
            workbookDataStore.activeChapterProperty.set(it)
        }
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