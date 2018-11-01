package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class ProgressOverlay : BorderPane() {

    var iconProperty = SimpleObjectProperty<Node>(VBox())
    var icon by iconProperty

    var textProperty = SimpleStringProperty()
    var text by textProperty

    init {
        style {
            alignment = Pos.CENTER
            backgroundColor += Color.BLACK
                    .deriveColor(0.0, 0.0, 0.0, 0.5)
        }

        center {
            vbox {
                alignment = Pos.CENTER
                stackpane {
                    alignment = Pos.CENTER
                    style {
                        prefWidth = 250.px
                        prefHeight = 250.px
                    }
                    hbox {
                        alignment = Pos.CENTER
                        add(iconProperty.value)
                        iconProperty.onChange {
                            this.clear()
                            stackpane {
                                add(iconProperty.value)
                            }
                        }
                    }
                    progressindicator {
                        style {
                            maxWidth = 125.px
                            maxHeight = 125.px
                            progressColor = Color.WHITE
                        }
                    }
                }
                label(textProperty) {
                    alignment = Pos.TOP_CENTER
                    textAlignment = TextAlignment.CENTER
                    style {
                        fontWeight = FontWeight.BOLD
                        fontSize =24.px
                    }
                }
            }
        }
    }
}

fun Pane.progressOverlay(init: ProgressOverlay.() -> Unit = {}): ProgressOverlay {
    val progressOverlay = ProgressOverlay()
    progressOverlay.init()
    add(progressOverlay)
    return progressOverlay
}