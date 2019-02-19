package org.wycliffeassociates.otter.jvm.app.widgets.card

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.GaussianBlur
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import tornadofx.*

class InnerCard(cardGraphic: Node? = null) : VBox() {
    val titleProperty = SimpleStringProperty()
    var title by titleProperty

    val bodyTextProperty = SimpleStringProperty()
    var bodyText by bodyTextProperty

    val majorLabelProperty = SimpleStringProperty()
    var majorLabel by majorLabelProperty

    val minorLabelProperty = SimpleStringProperty()
    var minorLabel by minorLabelProperty


    init {
        importStylesheet<DefaultStyles>()
        addClass(DefaultStyles.defaultInnerCard)
        stackpane {
            if(cardGraphic != null) {
            add(cardGraphic).apply {
                style{
                    backgroundColor+= Color.LIGHTGRAY
                }
            }}
            vbox {
                alignment = Pos.BOTTOM_CENTER
                stackpane {
                    ellipse {
                        centerX = 0.0
                        centerY = 0.0
                        radiusX = 50.0
                        radiusY = 10.0
                        fill = Color.WHITE
                        effect = GaussianBlur(15.0)
                        visibleProperty().bind(titleProperty.booleanBinding { it != null })

                    }
                    label(titleProperty) {
                        addClass(DefaultStyles.defaultTitle)
                    }
                }
                stackpane {
                    ellipse {
                        centerX = 50.0
                        centerY = 0.0
                        radiusX = 20.0
                        radiusY = 20.0
                        fill = Color.WHITE
                        effect = GaussianBlur(15.0)
                        visibleProperty().bind(bodyTextProperty.booleanBinding { it != null })
                    }
                    label(bodyTextProperty) {
                        addClass(DefaultStyles.defaultBody)
                    }
                }
                label(majorLabelProperty) {
                    addClass(DefaultStyles.defaultMajorLabel)
                }
                label(minorLabelProperty) {
                    addClass(DefaultStyles.defaultMinorLabel)
                }
                progressbar(0.2) { addClass(DefaultStyles.defaultCardProgressBar) }
            }
        }
    }

}

fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
    val ic = InnerCard(cardGraphic)
    ic.init()
    return ic
}