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
            if (it.hasSelectedTake) {
                versecard(it) {
                    actionButton.apply {
                        graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                        text = messages["record"]
                        style {
                            backgroundColor += c(Colors["base"])
                            textFill = c(Colors["primary"])
                            borderColor += box(c(Colors["primary"]))
                        }
                    }
                }
            } else {
                versecard(it) {
                    actionButton.apply {
                        graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                        text = messages["record"]
                        style {
                            backgroundColor += c(Colors["primary"])
                        }
                    }
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
                var context: Contexts
                viewModel.selectedTab.subscribeBy( //use observer to update verse cards in view when context changes
                        onNext = {
                            clear()
                            context = it
                            datagrid(viewModel.verses) {
                                vgrow = Priority.ALWAYS
                                cellCache {
                                    when (context) {
                                        Contexts.RECORD -> {
                                            if (it.hasSelectedTake) {
                                                versecard(it) {
                                                    actionButton.apply {
                                                        graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                                                        text = messages["record"]
                                                        style {
                                                            backgroundColor += c(Colors["base"])
                                                            textFill = c(Colors["primary"])
                                                            borderColor += box(c(Colors["primary"]))
                                                        }
                                                    }
                                                }
                                            } else {
                                                versecard(it) {
                                                    actionButton.apply {
                                                        graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                                                        text = messages["record"]
                                                        style {
                                                            backgroundColor += c(Colors["primary"])
                                                        }
                                                    }
                                                }

                                            }

                                        }
                                        Contexts.VIEW_TAKES -> {
                                            if (it.hasSelectedTake) {
                                                versecard(it) {
                                                    actionButton.apply {
                                                        graphic = MaterialIconView(MaterialIcon.APPS)
                                                        text = messages["viewTakes"]
                                                        style {
                                                            backgroundColor += c(Colors["secondary"])
                                                        }
                                                    }
                                                }
                                            } else {
                                                versecard(it) {
                                                    actionButton.hide()
                                                    style {
                                                        backgroundColor += c(Colors["baseBackground"])
                                                    }
                                                }

                                            }
                                        }
                                        Contexts.EDIT_TAKES -> {
                                            if (it.hasSelectedTake) {
                                                versecard(it) {
                                                    actionButton.apply {
                                                        graphic = MaterialIconView(MaterialIcon.EDIT)
                                                        text = messages["edit"]
                                                        style {
                                                            backgroundColor += c(Colors["tertiary"])
                                                        }
                                                    }
                                                }
                                            } else {
                                                versecard(it) {
                                                    actionButton.hide()
                                                    style {
                                                        backgroundColor += c(Colors["baseBackground"])
                                                    }
                                                }

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
                            viewModel.changeContext(Contexts.RECORD)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.APPS, "25px")
                        style {
                            backgroundColor += c(Colors["secondary"])
                        }
                        action {
                            viewModel.changeContext(Contexts.VIEW_TAKES)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.EDIT, "25px")
                        style {
                            backgroundColor += c(Colors["tertiary"])
                        }
                        action {
                            viewModel.changeContext(Contexts.EDIT_TAKES)
                        }
                    })
            add(activitypanel(tabList) {})
        }
    }
}


