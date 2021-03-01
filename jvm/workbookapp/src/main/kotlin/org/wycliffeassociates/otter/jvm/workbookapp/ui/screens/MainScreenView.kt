package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.Property
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.controls.card.InnerCard
import org.wycliffeassociates.otter.jvm.controls.navigation.projectnav
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.NavBoxType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.MainScreenStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.MainScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookViewModel
import tornadofx.*

class MainScreenView : View() {
    override val root = hbox {}
    val viewModel: MainScreenViewModel by inject()
    val workbookViewModel: WorkbookViewModel by inject()
    private val headerScalingFactor = 0.66
    private val chromeableStage = find<ChromeableStage>(
        mapOf(
            ChromeableStage::chrome to listMenu(),
            ChromeableStage::headerScalingFactor to headerScalingFactor
        )
    )

    data class NavBoxItem(val defaultText: String, val textGraphic: Node, val cardGraphic: Node, val type: NavBoxType)

    val navboxList: List<NavBoxItem> = listOf(
        NavBoxItem(
            messages["selectBook"],
            AppStyles.bookIcon("25px"),
            AppStyles.projectGraphic(), NavBoxType.PROJECT
        ),
        NavBoxItem(
            messages["selectChapter"],
            AppStyles.chapterIcon("25px"),
            AppStyles.chapterGraphic(),
            NavBoxType.CHAPTER
        ),
        NavBoxItem(
            messages["selectVerse"],
            AppStyles.verseIcon("25px"),
            AppStyles.chunkGraphic(), NavBoxType.CHUNK
        )
    )

    init {
        importStylesheet<MainScreenStyles>()
        with(root) {
            addClass(MainScreenStyles.main)
            style {
                backgroundColor += AppTheme.colors.defaultBackground
            }
            add(
                projectnav {
                    style {
                        prefWidth = 200.px
                        minWidth = 200.px
                    }
                    navboxList.forEach {
                        navbox(it.defaultText, it.textGraphic) {
                            innercard(it.cardGraphic) {
                                when (it.type) {
                                    NavBoxType.PROJECT -> {
                                        majorLabelProperty.bind(viewModel.selectedProjectName)
                                        minorLabelProperty.bind(viewModel.selectedProjectLanguage)
                                        visibleOnPropertyNotNull(workbookViewModel.activeWorkbookProperty)
                                        workbookViewModel.activeWorkbookProperty.onChange { workbook ->
                                            workbook?.let {
                                                graphicPathProperty.value = workbook.coverArtAccessor.getArtwork()
                                            }
                                        }
                                    }
                                    NavBoxType.CHAPTER -> {
                                        titleProperty.bind(viewModel.selectedChapterTitle)
                                        bodyTextProperty.bind(viewModel.selectedChapterBody)
                                        visibleOnPropertyNotNull(workbookViewModel.activeChapterProperty)
                                    }
                                    NavBoxType.CHUNK -> {
                                        titleProperty.bind(viewModel.selectedChunkTitle)
                                        bodyTextProperty.bind(viewModel.selectedChunkBody)
                                        visibleOnPropertyNotNull(workbookViewModel.activeChunkProperty)
                                    }
                                }
                            }
                        }
                    }
                    navButton {
                        text = messages["back"]
                        graphic = AppStyles.backIcon()
                        enableWhen(chromeableStage.canNavigateBackProperty)
                        setOnMouseClicked {
                            if (chromeableStage.canNavigateBackProperty.value) {
                                chromeableStage.back()
                            }
                            it.consume()
                        }
                    }
                }
            )

            chromeableStage.root.apply {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
            }
            chromeableStage.navigateTo(TabGroupType.PROJECT)
            add(chromeableStage.root)
        }
    }

    private fun listMenu(): Node {
        return ListMenu().apply {
            orientation = Orientation.HORIZONTAL
        }
    }

    private fun <T> InnerCard.visibleOnPropertyNotNull(property: Property<T>) {
        visibleProperty()
            .bind(property.booleanBinding { it != null })
    }
}
