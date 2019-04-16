package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.widgets.highlightablebutton.highlightablebutton
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceCardItem
import org.wycliffeassociates.otter.jvm.statusindicator.control.StatusIndicator
import org.wycliffeassociates.otter.jvm.statusindicator.control.statusindicator
import tornadofx.*
import tornadofx.FX.Companion.messages

class ResourceCard(private val resource: ResourceCardItem) : HBox() {

    val isCurrentResourceProperty = SimpleBooleanProperty(false)
    var primaryColorProperty = SimpleObjectProperty<Color>(Color.ORANGE)
    var primaryColor: Color by primaryColorProperty

    init {
        isFillHeight = false
        alignment = Pos.CENTER_LEFT
        maxHeight = 50.0

        vbox {
            spacing = 3.0
            hbox {
                spacing = 3.0
                add(
                    statusindicator {
                        initForResourceCard()
                        progress = 1.0
                    }
                )
                add(
                    statusindicator {
                        initForResourceCard()
                        progress = 0.0
                    }
                )
            }
            text(resource.title)
            maxWidth = 150.0
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
                maxWidth = 500.0
                text = messages["viewRecordings"]
            }
        )
    }

    private fun StatusIndicator.initForResourceCard() {
        prefWidth = 75.0
        primaryFillProperty.bind(primaryColorProperty)
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 3.0
    }

}

fun resourcecard(resource: ResourceCardItem, init: ResourceCard.() -> Unit = {}): ResourceCard {
    val rc = ResourceCard(resource)
    rc.init()
    return rc
}
