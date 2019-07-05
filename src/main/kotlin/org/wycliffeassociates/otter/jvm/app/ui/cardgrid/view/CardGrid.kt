package org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.viewmodel.ContentGridViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.app.widgets.card.card
import tornadofx.*

class CardGrid : Fragment() {
    private val viewModel: ContentGridViewModel by inject()

    init {
        importStylesheet<CardGridStyles>()
        importStylesheet<DefaultStyles>()

    }

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        addClass(AppStyles.appBackground)
        progressindicator {
            visibleProperty().bind(viewModel.loadingProperty)
            managedProperty().bind(visibleProperty())
            addClass(CardGridStyles.contentLoadingProgress)
        }

        datagrid(viewModel.filteredContent) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            isFillWidth = true
            addClass(AppStyles.appBackground)
            addClass(CardGridStyles.contentContainer)
            vgrow = Priority.ALWAYS
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
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cardGraphic() : Node {
        if(viewModel.filteredContent.first().dataType == "content") {
            return AppStyles.chunkGraphic()
        }
        return AppStyles.chapterGraphic()
    }
}
