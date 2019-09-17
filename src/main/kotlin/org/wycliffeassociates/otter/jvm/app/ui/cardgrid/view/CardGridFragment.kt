package org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardData
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.viewmodel.CardGridViewModel
import org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.app.widgets.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.app.widgets.card.card
import tornadofx.*

class CardGridFragment : Fragment() {
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
            navigator.navigateTo(TabGroupType.RECORDABLE)
        } else if (item.chunkSource != null) {
            navigator.navigateTo(TabGroupType.RECORD_SCRIPTURE)
        }
    }

    private fun cardGraphic(): Node {
        if (viewModel.filteredContent.first().dataType == "content") {
            return AppStyles.chunkGraphic()
        }
        return AppStyles.chapterGraphic()
    }
}
