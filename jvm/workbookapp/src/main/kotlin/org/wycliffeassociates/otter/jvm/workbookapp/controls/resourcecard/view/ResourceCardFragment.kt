package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.button.highlightablebutton
import org.wycliffeassociates.otter.jvm.controls.statusindicator.StatusIndicator
import org.wycliffeassociates.otter.jvm.controls.statusindicator.statusindicator
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RecordResourcePage
import tornadofx.*

class ResourceCardFragment(
    private val item: ResourceCardItem,
    private val filterCompletedCardsProperty: BooleanProperty,
    private val navigator: NavigationMediator
) : Fragment() {
    override val root = HBox()
    val isCurrentResourceProperty = SimpleBooleanProperty(false)
    var primaryColorProperty = SimpleObjectProperty<Color>(Color.ORANGE)
    var primaryColor: Color by primaryColorProperty

    init {
        root.apply {
            alignment = Pos.CENTER_LEFT

            hiddenWhen { item.cardCompletedBinding().and(filterCompletedCardsProperty) }
            managedWhen { visibleProperty() }

            vbox {
                spacing = 3.0
                hbox {
                    spacing = 3.0
                    add(
                        statusindicator {
                            initForResourceCard()
                            progressProperty.bind(item.titleProgressProperty)
                        }
                    )
                    add(
                        statusindicator {
                            initForResourceCard()
                            item.bodyProgressProperty?.let { progressProperty.bind(it) }
                            isVisible = item.hasBodyAudio
                        }
                    )
                }
                text(item.title) {
                    wrappingWidthProperty().bind(root.widthProperty().divide(1.5))
                }
            }

            region {
                hgrow = Priority.ALWAYS
            }

            add(
                highlightablebutton {
                    highlightColorProperty.bind(primaryColorProperty)
                    secondaryColor = Color.WHITE
                    isHighlightedProperty.bind(isCurrentResourceProperty)
                    graphic = MaterialIconView(MaterialIcon.APPS, "25px")
                    text = messages["viewRecordings"]
                    action {
                        item.onSelect()
                        navigator.dock<RecordResourcePage>()
                    }
                }
            )
        }
    }

    private fun StatusIndicator.initForResourceCard() {
        prefWidth = 75.0
        primaryFillProperty.bind(primaryColorProperty)
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 3.0
    }
}

fun resourceCardFragment(
    resource: ResourceCardItem,
    filterCompletedCardsProperty: BooleanProperty,
    navigator: NavigationMediator,
    init: ResourceCardFragment.() -> Unit = {}
) = ResourceCardFragment(resource, filterCompletedCardsProperty, navigator).apply { init.invoke(this) }
