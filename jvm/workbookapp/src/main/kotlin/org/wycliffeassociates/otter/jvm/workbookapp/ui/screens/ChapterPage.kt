package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.card.card
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ChapterBanner
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.CardGridStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class ChapterPage : Fragment() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: ChapterPageViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val banner: ChapterBanner
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeChunkProperty.stringBinding {
                it?.let {
                    MessageFormat.format(
                        messages["chunkTitle"],
                        messages[ContentLabel.of(it.contentType).value],
                        it.start
                    )
                } ?: messages["chunk"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK))
        onClickAction {
            navigator.dock(this@ChapterPage)
        }
    }

    override fun onDock() {
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)
    }

    init {
        banner = ChapterBanner().apply {
            viewModel.chapterCard.onChangeAndDoNow {
                it?.let {
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
                                navigator.dock<RecordScriptureFragment>()
                            }
                        }
                }
            }
        }
    }

    override val root = vbox {
        add(banner)
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
                                navigator.dock<RecordScriptureFragment>()
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
