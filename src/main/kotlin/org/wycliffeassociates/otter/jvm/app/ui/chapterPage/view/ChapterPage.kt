package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Border
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.*
import tornadofx.*

class ChapterPage : View() {
    private val viewModel: ChapterPageViewModel by inject()
    private val defaultGrid = datagrid(viewModel.verses) {
        cellCache {
            if(it.hasSelectedTake) {
                versecard {
                    title = messages["verses"]
                    selectedTake = it.verseNumber
                }
            }
                else {
                    versecard{

                    }
                }

            }
        }

    override val root = hbox {
        vbox {
            label(viewModel.bookTitle) {
                prefHeight = 100.0
            }
            listview(viewModel.chapters) {
                vgrow = Priority.ALWAYS
                addEventFilter(MouseEvent.MOUSE_CLICKED) {
                    viewModel.selectedChapter(viewModel.chapters.indexOf(this.selectedItem) + 1)
                }
            }
        }

        vbox {
            hgrow = Priority.ALWAYS
            vbox {
                add(defaultGrid)
                var context: String
                viewModel.selectedTab.subscribeBy( //use observer to update verse cards in view when context changes
                        onNext = {
                            clear()
                            context = it
                            datagrid(viewModel.verses) {
                                vgrow = Priority.ALWAYS
                                cellCache {
                                    when (context) {
                                        Contexts.RECORD.label -> {
                                            versecard {
                                                title = messages["verses"]
                                                selectedTake = it.verseNumber
                                            }
                                        }
                                        Contexts.VIEW_TAKES.label -> {
                                            versecard {
                                                title = messages["verses"]
                                                selectedTake = it.verseNumber
                                            }
                                        }
                                        Contexts.EDIT_TAKES.label -> {
                                            versecard {
                                                title = messages["verses"]
                                                selectedTake = it.verseNumber
                                            }
                                        }
                                        else -> {
                                            versecard{
                                                title = messages["verses"]
                                                selectedTake = it.verseNumber
                                            }
                                        }
                                    }
                                }
                            }
                        }
                )
            }
            val tabList = arrayListOf(
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
                        style {
                            backgroundColor += c(Colors["primary"])
                        }
                        action {
                            viewModel.changeContext(Contexts.RECORD.label)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.APPS, "25px")
                        style {
                            backgroundColor += c(Colors["secondary"])
                        }
                        action {
                            viewModel.changeContext(Contexts.VIEW_TAKES.label)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.EDIT, "25px")
                        style {
                            backgroundColor += c(Colors["tertiary"])
                        }
                        action{
                            viewModel.changeContext(Contexts.EDIT_TAKES.label)
                        }
                    })
            add(activitypanel {
                tabs.onNext(tabList)
            }
            )

        }
    }
}


