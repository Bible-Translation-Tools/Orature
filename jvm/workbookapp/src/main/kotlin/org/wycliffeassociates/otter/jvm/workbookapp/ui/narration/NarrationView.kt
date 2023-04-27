package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class NarrationView(): View() {
    override val root = stackpane {
        borderpane {
            top<NarrationHeader>()

        }
    }
}

class NarrationHeader(): View() {
    private val viewModel by inject<NarrationHeaderViewModel>()

    override val root = hbox {
        narrationTitle(viewModel.titleProperty)
        menubutton {
            addClass("btn", "btn--primary", "btn--borderless", "wa-menu-button")
            graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)

            item(messages["undoAction"]) {
                graphic = FontIcon(MaterialDesign.MDI_UNDO)
                // action { viewModel.onUndoAction() }

                enableWhen(viewModel.narrationHistory.hasUndoProperty)
            }
            item(messages["redoAction"]) {
                graphic = FontIcon(MaterialDesign.MDI_REDO)
                // action { viewModel.onRedoAction() }

                enableWhen(viewModel.narrationHistory.hasRedoProperty)
            }
            item(messages["openChapterIn"]) {
                graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                // action { viewModel.onChapterOpenIn() }

//                disableProperty().bind(workbookDataStore.activeChapterProperty.booleanBinding {
//                    it?.audio?.getAllTakes()?.firstOrNull { take ->
//                        take.deletedTimestamp.value?.value == null
//                    } == null
//                })
            }
            item(messages["editVerseMarkers"]) {
                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                //action { viewModel.onEditVerseMarkers() }

                disableProperty().bind(workbookDataStore.activeChapterProperty.booleanBinding {
                    it?.audio?.getAllTakes()?.firstOrNull { take ->
                        take.deletedTimestamp.value?.value == null
                    } == null
                })
            }
            item(messages["restartChapter"]) {
                graphic = FontIcon(MaterialDesign.MDI_DELETE)
                action { viewModel.onChapterReset() }
            }
        }

        add(
            ChapterSelector().apply {
                chapterTitleProperty.bind(workbookDataStore.activeChapterProperty.stringBinding {
                    it?.let {
                        MessageFormat.format(
                            messages["chapterTitle"],
                            messages["chapter"],
                            it.sort
                        )
                    } ?: ""
                })

                prevDisabledProperty.bind(viewModel.hasPreviousChapter.not())
                nextDisabledProperty.bind(viewModel.hasNextChapter.not())

                setOnPreviousChapter {
                    viewModel.previousChapter()
                }
                setOnNextChapter {
                    viewModel.nextChapter()
                }
            }
        )
    }
    }
}

class NarrationHeaderViewModel(): ViewModel() {
    // private val workbookDataStore by inject<WorkbookDataStore>()

    val titleProperty = SimpleStringProperty("Narration Title")
}

class NarrationTitle(val titleTextProperty: ObservableValue<String> = SimpleStringProperty()): HBox() {
    constructor(titleText: String): this(SimpleStringProperty(titleText))

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