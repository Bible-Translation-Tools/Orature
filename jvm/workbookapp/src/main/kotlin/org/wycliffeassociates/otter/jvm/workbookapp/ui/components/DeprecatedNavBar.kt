package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.Property
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.card.InnerCard
import org.wycliffeassociates.otter.jvm.controls.navigation.projectnav
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.NavBoxType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.MainScreenStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.MainScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

private class NavBoxItem(
    val defaultText: String,
    val textGraphic: Node,
    val cardGraphic: Node,
    val type: NavBoxType
)

class DeprecatedNavBar: View() {

    private val viewModel: MainScreenViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    init {
        importStylesheet<MainScreenStyles>()
    }

    private val navboxList: List<NavBoxItem> = listOf(
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

    override val root = projectnav {
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
                            visibleOnPropertyNotNull(workbookDataStore.activeWorkbookProperty)
                            workbookDataStore.activeWorkbookProperty.onChange { workbook ->
                                workbook?.let {
                                    graphicPathProperty.value = workbook.coverArtAccessor.getArtwork()
                                }
                            }
                        }
                        NavBoxType.CHAPTER -> {
                            titleProperty.bind(viewModel.selectedChapterTitle)
                            bodyTextProperty.bind(viewModel.selectedChapterBody)
                            visibleOnPropertyNotNull(workbookDataStore.activeChapterProperty)
                        }
                        NavBoxType.CHUNK -> {
                            titleProperty.bind(viewModel.selectedChunkTitle)
                            bodyTextProperty.bind(viewModel.selectedChunkBody)
                            visibleOnPropertyNotNull(workbookDataStore.activeChunkProperty)
                        }
                    }
                }
            }
        }
        navButton {
            text = messages["back"]
            graphic = AppStyles.backIcon()
            enableWhen(workspace.backButton.disabledProperty().not())
            action {
                workspace.navigateBack()
            }
        }
    }

    private fun <T> InnerCard.visibleOnPropertyNotNull(property: Property<T>) {
        visibleProperty()
            .bind(property.booleanBinding { it != null })
    }
}
