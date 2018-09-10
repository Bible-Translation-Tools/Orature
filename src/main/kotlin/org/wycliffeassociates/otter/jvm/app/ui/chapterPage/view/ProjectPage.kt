package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Chapter
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Verse
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel.ProjectPageViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.*
import tornadofx.*

class ProjectPage : View() {
    private val viewModel: ProjectPageViewModel by inject()
    private var verseGrid = createDataGrid()

    override val root = hbox {
        vbox {
            label {
                prefHeight = 100.0
                textProperty().bind(viewModel.bookTitleProperty)
            }
            listview<Chapter> {
                itemsProperty().bind(viewModel.chaptersProperty)
                cellCache {
                    label(it.number.toString())
                }
                vgrow = Priority.ALWAYS
                selectionModel.selectedItems.onChange {
                    viewModel.selectChapter(it.list.first().number)
                }
            }
        }

        vbox {
            hgrow = Priority.ALWAYS
            vbox {
                viewModel.contextProperty.onChange {
                    verseGrid.removeFromParent()
                    verseGrid = createDataGrid()
                    add(verseGrid)
                }
                add(verseGrid)
            }
            val tabList = arrayListOf(
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
                        style {
                            backgroundColor += c(Colors["primary"])
                        }
                        action {
                            viewModel.changeContext(ChapterContext.RECORD)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.APPS, "25px")
                        style {
                            backgroundColor += c(Colors["secondary"])
                        }
                        action {
                            viewModel.changeContext(ChapterContext.VIEW_TAKES)
                        }
                    },
                    activityTab {
                        graphic = MaterialIconView(MaterialIcon.EDIT, "25px")
                        style {
                            backgroundColor += c(Colors["tertiary"])
                        }
                        action {
                            viewModel.changeContext(ChapterContext.EDIT_TAKES)
                        }
                    })
            add(activitypanel(tabList) {})
        }
    }

    private fun createDataGrid(): DataGrid<Verse> {
        val dataGrid = DataGrid<Verse>()
        with(dataGrid) {
            itemsProperty.bind(viewModel.visibleVersesProperty)
            vgrow = Priority.ALWAYS
            cellCache {
                val verseCard = VerseCard(it)
                when (viewModel.contextProperty.value) {
                    ChapterContext.RECORD -> {
                        with(verseCard) {
                            actionButton.apply {
                                graphic = MaterialIconView(MaterialIcon.MIC_NONE)
                                text = messages["record"]
                                style {
                                    if (hasSelectedTake) {
                                        backgroundColor += c(Colors["base"])
                                        textFill = c(Colors["primary"])
                                        borderColor += box(c(Colors["primary"]))
                                    } else {
                                        backgroundColor += c(Colors["primary"])
                                    }
                                }
                            }
                        }
                    }
                    ChapterContext.VIEW_TAKES -> {
                        with(verseCard) {
                            actionButton.apply {
                                if (hasSelectedTake) {
                                    graphic = MaterialIconView(MaterialIcon.APPS)
                                    text = messages["viewTakes"]
                                    style {
                                        backgroundColor += c(Colors["secondary"])
                                    }
                                } else {
                                    actionButton.hide()
                                    style {
                                        backgroundColor += c(Colors["baseBackground"])
                                    }
                                }
                            }
                        }
                    }
                    ChapterContext.EDIT_TAKES -> {
                        with(verseCard) {
                            if (hasSelectedTake) {
                                actionButton.apply {
                                    graphic = MaterialIconView(MaterialIcon.EDIT)
                                    text = messages["edit"]
                                    style {
                                        backgroundColor += c(Colors["tertiary"])
                                    }
                                }
                            } else {
                                actionButton.hide()
                                style {
                                    backgroundColor += c(Colors["baseBackground"])
                                }
                            }
                        }
                    }
                }
                verseCard
            }
        }
        return dataGrid
    }
}


