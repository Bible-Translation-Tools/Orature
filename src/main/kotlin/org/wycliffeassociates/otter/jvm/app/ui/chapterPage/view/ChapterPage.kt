package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.geometry.Pos
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Border
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.ActivityPanel
import org.wycliffeassociates.otter.jvm.app.widgets.VerseCard
import tornadofx.*

class ChapterPage : View() {
    private val viewModel: ChapterPageViewModel by inject()
    private val defaultGrid = datagrid(viewModel.verses) {
        cellCache {
            VerseCard(it,Colors["primary"], MaterialIcon.MIC_NONE, messages["record"], false)
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
                    viewModel.selectedChapter(viewModel.chapters.indexOf(this.selectedItem))
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
                                        "record" -> VerseCard(it,Colors["primary"], MaterialIcon.MIC_NONE, messages["record"], false)
                                        "viewtakes" -> VerseCard(it,Colors["secondary"], MaterialIcon.APPS, messages["viewTakes"], true)
                                        "edit" -> VerseCard(it,Colors["tertiary"], MaterialIcon.EDIT, messages["edit"], true)
                                        else -> VerseCard(it,Colors["primary"], MaterialIcon.MIC_NONE, messages["record"], false)
                                    }
                                }
                            }
                        }
                )
            }

            add(ActivityPanel(MaterialIconView(MaterialIcon.MIC_NONE, "25px"),c(Colors["primary"]),
                    MaterialIconView(MaterialIcon.APPS, "25px"),c(Colors["secondary"]),
                    MaterialIconView(MaterialIcon.EDIT, "25px"),c(Colors["tertiary"]),
                    MaterialIconView(MaterialIcon.BLOCK, "25px"), c(Colors["baseBackground"])).apply {

                alignment = Pos.CENTER
                buttonLeft.action {
                    viewModel.changeContext("record")
                }
                buttonCenterLeft.action {
                    viewModel.changeContext("viewtakes")
                }
                buttonCenterRight.action {
                    viewModel.changeContext("edit")
                }
                buttonRight.action {

                }
            })
        }
    }
}


