package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class ProgressDialog : Fragment() {

    val graphicProperty = SimpleObjectProperty<Node>(VBox())
    var graphic by graphicProperty

    val textProperty = SimpleStringProperty()
    var text by textProperty

    override val root = borderpane {
        addClass(WidgetsStyles.progressDialog)
        center {
            stackpane {
                progressindicator()
                hbox {
                    alignment = Pos.CENTER
                    add(graphic)
                    graphicProperty.onChange {
                        clear()
                        add(graphic)
                    }
                }
            }
        }
        bottom {
            label(textProperty) {
                visibleProperty().bind(textProperty().isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL)
    }
}

fun EventTarget.progressdialog(setup: ProgressDialog.() -> Unit = {}): ProgressDialog {
    val progressDialog = ProgressDialog()
    progressDialog.setup()
    return progressDialog
}