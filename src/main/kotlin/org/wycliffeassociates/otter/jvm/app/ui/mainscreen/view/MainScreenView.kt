package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.*
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.NavBoxType
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.viewmodel.MainViewViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectgrid.view.ProjectGridView
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.projectnav.projectnav
import tornadofx.*

class MainScreenView : View() {
    override val root = hbox {}
    var activeFragment: Workspace = Workspace()
    var fragmentStage: AnchorPane by singleAssign()

    val viewModel: MainViewViewModel by inject()
    val workbookViewModel: WorkbookViewModel by inject()

    data class NavBoxItem(val defaultText: String, val textGraphic: Node, val cardGraphic: Node, val type: NavBoxType)

    val navboxList: List<NavBoxItem> = listOf(
            NavBoxItem(messages["selectBook"],AppStyles.bookIcon("25px"), AppStyles.projectGraphic(), NavBoxType.PROJECT),
            NavBoxItem(messages["selectChapter"], AppStyles.chapterIcon("25px"), AppStyles.chapterGraphic(), NavBoxType.CHAPTER),
            NavBoxItem(messages["selectVerse"], AppStyles.verseIcon("25px"), AppStyles.chunkGraphic(), NavBoxType.CHUNK))

    init {
        importStylesheet<MainScreenStyles>()
        activeFragment.header.removeFromParent()
        with(root) {
            addClass(MainScreenStyles.main)
            style {
                backgroundColor += AppTheme.colors.defaultBackground
            }
            add(projectnav {
                style {
                    prefWidth = 200.px
                    minWidth = 200.px
                }
                navboxList.forEach {
                    navbox(it.defaultText, it.textGraphic){
                        innercard(it.cardGraphic){
                            when(it.type) {
                                NavBoxType.PROJECT -> {
                                    majorLabelProperty.bind(viewModel.selectedProjectName)
                                    minorLabelProperty.bind(viewModel.selectedProjectLanguage)
                                    visibleProperty()
                                        .bind(workbookViewModel.activeWorkbookProperty.booleanBinding { it != null })
                                }
                                NavBoxType.CHAPTER -> {
                                    titleProperty.bind(viewModel.selectedChapterTitle)
                                    bodyTextProperty.bind(viewModel.selectedChapterBody)
                                    visibleProperty()
                                        .bind(workbookViewModel.activeChapterProperty.booleanBinding { it != null })
                                }
                                NavBoxType.CHUNK -> {
                                    titleProperty.bind(viewModel.selectedChunkTitle)
                                    bodyTextProperty.bind(viewModel.selectedChunkBody)
                                    visibleProperty()
                                        .bind(workbookViewModel.activeChunkProperty.booleanBinding { it != null })
                                }
                            }
                        }
                    }
                }
                navButton {
                    text = messages["back"]
                    graphic = AppStyles.backIcon()
                    action {
                        navigateBack()
                    }
                }
            })

            fragmentStage = anchorpane {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                add(listmenu {
                    orientation = Orientation.HORIZONTAL
                    item(messages["home"], MaterialIconView(MaterialIcon.HOME, "20px"))
                    item(messages["profile"], MaterialIconView(MaterialIcon.PERSON, "20px"))
                    item(messages["settings"], MaterialIconView(MaterialIcon.SETTINGS, "20px"))

                    anchorpaneConstraints {
                        topAnchor = 0
                        rightAnchor = 0
                    }
                })
                borderpane {
                    anchorpaneConstraints {
                        topAnchor = 55
                        leftAnchor = 0
                        rightAnchor = 0
                        bottomAnchor = 0
                    }

                    center {
                        activeFragment.dock<ProjectGridView>()
                        add(activeFragment)
                    }
                }
            }
        }
    }

    private fun navigateBack() {
        //navigate back to verse selection from viewing takes
        if (workbookViewModel.activeChunkProperty.value != null) {
            workbookViewModel.activeChunkProperty.value = null
            activeFragment.navigateBack()
        }
        //from verse selection, navigate back to chapter selection
        else if (workbookViewModel.activeChapterProperty.value != null) {
            workbookViewModel.activeChapterProperty.set(null)
        }

        else activeFragment.navigateBack()
    }
}