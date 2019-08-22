package org.wycliffeassociates.otter.jvm.app.widgets.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.GaussianBlur
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
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

    val showProgressProperty = SimpleBooleanProperty(false)
    var showProgress by showProgressProperty

    val progressProperty = SimpleDoubleProperty(0.0)
    var progress by progressProperty

    val selectedExistsProperty = SimpleBooleanProperty(false)
    var selectedExists by selectedExistsProperty

    init {
        importStylesheet<DefaultStyles>()
        addClass(DefaultStyles.defaultInnerCard)
        stackpane {
            if (cardGraphic != null) {
                add(cardGraphic).apply {
                    style {
                        backgroundColor += Color.LIGHTGRAY
                    }
                }
            }
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
                    graphic = DefaultStyles.checkCircle("25px").apply {
                        fill = DefaultStyles.green()
                    }
                    graphic.managedProperty().bind(selectedExistsProperty.booleanBinding { it != false })
                    graphic.visibleProperty().bind(selectedExistsProperty.booleanBinding { it != false })
                    addClass(DefaultStyles.defaultMinorLabel)
                }
                progressbar(progressProperty) {
                    addClass(DefaultStyles.defaultCardProgressBar)
                    managedProperty().bind(showProgressProperty.booleanBinding { it != false })
                    visibleProperty().bind(showProgressProperty.booleanBinding { it != false })
                    toggleClass(DefaultStyles.completedProgress, progressProperty
                        .booleanBinding { it != null && 0.9999 <= it.toDouble() })
                }
            }
        }
    }
}

fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
    val ic = InnerCard(cardGraphic)
    ic.init()
    return ic
}