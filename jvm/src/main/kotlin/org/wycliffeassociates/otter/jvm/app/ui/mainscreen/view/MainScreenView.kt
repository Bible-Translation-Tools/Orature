package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.Property
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.*
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.NavBoxType
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.viewmodel.MainScreenViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.controls.card.InnerCard
import org.wycliffeassociates.otter.jvm.controls.projectnav.projectnav
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
                        action {
                            chromeableStage.back()
                        }
                    }
                }
            )

            chromeableStage.root.apply {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS
            }
            add(chromeableStage.root)
        }
    }

    private fun listMenu(): Node {
        return ListMenu().apply {
            orientation = Orientation.HORIZONTAL
            item(messages["home"], MaterialIconView(MaterialIcon.HOME, "20px"))
            item(messages["profile"], MaterialIconView(MaterialIcon.PERSON, "20px"))
            item(messages["settings"], MaterialIconView(MaterialIcon.SETTINGS, "20px"))
        }
    }

    private fun <T> InnerCard.visibleOnPropertyNotNull(property: Property<T>) {
        visibleProperty()
            .bind(property.booleanBinding { it != null })
    }
}