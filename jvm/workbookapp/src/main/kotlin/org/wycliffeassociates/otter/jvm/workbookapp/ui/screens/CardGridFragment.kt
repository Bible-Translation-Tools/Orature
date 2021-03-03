package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.Priority
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ChapterBanner
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.CardGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.card.card
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.CardGridStyles
import tornadofx.*

class CardGridFragment : Fragment() {


}

class BookPage : Fragment() {
    private val navigator: ChromeableStage by inject()
    private val viewModel: CardGridViewModel by inject()

    init {
        importStylesheet<CardGridStyles>()
        importStylesheet<DefaultStyles>()
    }

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        alignment = Pos.CENTER
        addClass(AppStyles.whiteBackground)
        progressindicator {
            visibleProperty().bind(viewModel.loadingProperty)
            managedProperty().bind(visibleProperty())
            addClass(CardGridStyles.contentLoadingProgress)
        }

        datagrid(viewModel.filteredContent) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            isFillWidth = true
            addClass(AppStyles.whiteBackground)
            addClass(CardGridStyles.contentContainer)
            cellCache { item ->
                card {
                    addClass(DefaultStyles.defaultCard)
                    cardfront {
                        innercard(cardGraphic()) {
                            title = item.item.toUpperCase()
                            bodyText = item.bodyText
                        }
                        cardbutton {
                            addClass(DefaultStyles.defaultCardButton)
                            text = messages["openProject"]
                            graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                .apply { fill = AppTheme.colors.appRed }
                            onMousePressed = EventHandler {
                                viewModel.onCardSelection(item)
                                navigate(item)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigate(item: CardData) {
        if (item.chapterSource != null) {
            workspace.dock<ChapterPage>()
        } else if (item.chunkSource != null) {
            workspace.dock<RecordScriptureFragment>()
        }
    }

    private fun cardGraphic(): Node {
        if (viewModel.filteredContent.first().dataType == "content") {
            return AppStyles.chunkGraphic()
        }
        return AppStyles.chapterGraphic()
    }
}

class ChapterPage : Fragment() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: CardGridViewModel by inject()

    override val root = vbox {
        add(
            ChapterBanner().apply {
                visibleWhen(viewModel.chapterOpen)
                managedProperty().bind(viewModel.chapterOpen)
                if (viewModel.chapterCard.value != null) {
                    bookTitle.text = viewModel.workbookDataStore.workbook.target.title
                    chapterCount.text = viewModel.workbookDataStore.activeChapterProperty.value?.title
                    openButton.text = messages["open"]
                    viewModel
                        .workbookDataStore
                        .activeChapterProperty
                        .value
                        ?.let { chapter ->
                            chapter.chunks
                                .filter {
                                    it.contentType == ContentType.TEXT
                                }
                                .count()
                                .doOnError { e ->
                                    logger.error("Error in setting chapter banner chunk count", e)
                                }
                                .subscribe { count ->
                                    Platform.runLater {
                                        chunkCount.text = count.toString()
                                    }
                                }
                            openButton.setOnMouseClicked {
                                viewModel.onCardSelection(CardData(chapter))
                                workspace.dock<RecordScriptureFragment>()
                            }
                        }
                }
            }
        )
        datagrid(viewModel.filteredContent) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            isFillWidth = true
            addClass(AppStyles.whiteBackground)
            addClass(CardGridStyles.contentContainer)
            cellCache { item ->
                card {
                    addClass(DefaultStyles.defaultCard)
                    cardfront {
                        innercard(cardGraphic()) {
                            title = item.item.toUpperCase()
                            bodyText = item.bodyText
                        }
                        cardbutton {
                            addClass(DefaultStyles.defaultCardButton)
                            text = messages["openProject"]
                            graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                .apply { fill = AppTheme.colors.appRed }
                            onMousePressed = EventHandler {
                                viewModel.onCardSelection(item)
                                workspace.dock<RecordScriptureFragment>()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cardGraphic(): Node {
        if (viewModel.filteredContent.first().dataType == "content") {
            return AppStyles.chunkGraphic()
        }
        return AppStyles.chapterGraphic()
    }
}
