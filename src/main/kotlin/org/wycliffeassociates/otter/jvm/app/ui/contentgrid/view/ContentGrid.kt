package org.wycliffeassociates.otter.jvm.app.ui.contentgrid.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.Property
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.contentgrid.viewmodel.ContentGridViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.app.widgets.card.card
import tornadofx.*


class ContentGrid : Fragment() {
    private val viewModel: ContentGridViewModel by inject()

    val activeCollection: Property<Collection> = viewModel.activeCollectionProperty
    val activeProject: Property<Collection> = viewModel.activeProjectProperty
    val activeContent: Property<Content> = viewModel.activeContentProperty

    init {
        importStylesheet<ContentGridStyles>()
        importStylesheet<DefaultStyles>()
    }

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        addClass(AppStyles.appBackground)
        addClass(ContentGridStyles.panelStyle)

        progressindicator {
            visibleProperty().bind(viewModel.loadingProperty)
            managedProperty().bind(visibleProperty())
            addClass(ContentGridStyles.contentLoadingProgress)
        }
        scrollpane {
            isFitToHeight = true
            isFitToWidth = true
            flowpane {
                addClass(AppStyles.appBackground)
                addClass(ContentGridStyles.contentContainer)
                bindChildren(viewModel.filteredContent) {
                    card {
                        addClass(DefaultStyles.defaultCard)
                        cardfront {
                            innercard(AppStyles.chunkGraphic()) {
                                title = it.first.value.labelKey.toUpperCase()
                                bodyText = it.first.value.start.toString()
                            }
                            cardbutton {
                                addClass(DefaultStyles.defaultCardButton)
                                text = messages["openProject"]
                                graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                        .apply { fill = AppTheme.colors.appRed }
                                action {
                                    viewModel.viewContentTakes(it.first.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
